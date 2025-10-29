package redfox.hub.event

import cn.nukkit.Player
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.block.BlockBreakEvent
import cn.nukkit.event.block.BlockPlaceEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.ProjectileHitEvent
import cn.nukkit.event.entity.ProjectileLaunchEvent
import cn.nukkit.event.inventory.InventoryPickupItemEvent
import cn.nukkit.event.player.*
import cn.nukkit.level.Location
import cn.nukkit.level.Sound
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.SetHudPacket
import cn.nukkit.network.protocol.types.PlayerAbility
import cn.nukkit.network.protocol.types.hud.HudElement
import cn.nukkit.network.protocol.types.hud.HudVisibility
import redfox.hub.Core
import redfox.hub.database.Redis
import redfox.hub.forms.cosmetic.CosmeticForm
import redfox.hub.forms.games.GamesForm
import redfox.hub.forms.profile.ProfileForm
import redfox.hub.manager.ServerManager
import redfox.hub.model.ItemStorage
import redfox.hub.task.ArrowSoundTask
import redfox.hub.task.TeleportBowTask
import redfox.hub.transfer.Transfer
import redfox.hub.utils.Utils
import translate.TranslateAPI

class Listener : Listener {

    private val doubleJumped = mutableSetOf<String>()
    private val packetCounts = mutableMapOf<String, Int>()
    val teleportCooldown = mutableMapOf<String, Long>()

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player
        val lang = Redis.getPlayerLang(player.name)
        val name = player.name.lowercase()

        val allowedIp = ServerManager.opsWithIps[name]
        val playerIp = player.address

        if (allowedIp != null) {
            if (allowedIp != playerIp) {
                event.setKickMessage(TranslateAPI.get("event.kick.ip_mismatch", lang))
                event.isCancelled = true
                return
            }
        }
        ServerManager.playerIps[player.name.lowercase()] = playerIp
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val lang = Redis.getPlayerLang(player.name)

        Redis.setPlayerServer(player.name, "Hub")
        Transfer.spawnRandomLobby(player)

        val name = player.name.lowercase()
        val allowedIp = ServerManager.opsWithIps[name]
        val playerIp = player.address

        val packet = SetHudPacket()
        packet.visibility = HudVisibility.HIDE
        packet.elements.add(HudElement.HEALTH)
        packet.elements.add(HudElement.FOOD_BAR)
        player.setExperience(0, 1)
        player.dataPacket(packet)

        if (allowedIp != null && allowedIp == playerIp && !player.isOp) {
            player.isOp = true
            player.sendMessage(TranslateAPI.get("event.join.auto_op", lang))
        }

        player.setGamemode(2)
        event.setJoinMessage("")
        player.inventory.clearAll()
        ItemStorage.manager.giveItems(player)
        player.allowFlight = true
    }

    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        val player = event.player
        val lang = Redis.getPlayerLang(player.name)
        event.isCancelled = true
        Utils.sendWarning(player, TranslateAPI.get("event.chat.cannot_write", lang))
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        event.setQuitMessage("")
        doubleJumped.remove(player.name)
        Redis.deletePlayerServer(player.name)
    }
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val lang = Redis.getPlayerLang(player.name)

        val cosmeticName = ItemStorage.manager.getItem(0, lang)?.customName
        val playName = ItemStorage.manager.getItem(4, lang)?.customName
        val profileName = ItemStorage.manager.getItem(8, lang)?.customName

        val clickedName = event.item?.customName

        when (clickedName) {
            cosmeticName -> if (Utils.canOpenForm(player)) CosmeticForm.send(player)
            playName -> if (Utils.canOpenForm(player)) GamesForm.send(player)
            profileName -> if (Utils.canOpenForm(player)) ProfileForm.send(player)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        if (!player.isOnGround && player.allowFlight && player.adventureSettings.get(PlayerAbility.FLYING) && !doubleJumped.contains(player.name)) {
            val direction = player.directionVector.normalize().multiply(1.0)
            val jumpPower = 1.3

            val jumpVector = Vector3(
                direction.x,
                jumpPower,
                direction.z
            )

            player.motion = jumpVector

            player.adventureSettings.set(PlayerAbility.FLYING, false)
            player.adventureSettings.update()

            player.allowFlight = false
            doubleJumped.add(player.name)

            player.level.addSound(player, Sound.MOB_ENDERDRAGON_FLAP)
        }

        if (player.isOnGround) {
            player.allowFlight = true
            doubleJumped.remove(player.name)
        }
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        val lang = Redis.getPlayerLang(player.name)

        if (!player.isOp) {
            event.isCancelled = true
            Utils.sendWarning(player, TranslateAPI.get("event.command.no_permission", lang))
        }
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity is Player && event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.damager is Player && event.entity is Player) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onArrowHit(event: ProjectileHitEvent) {
        val projectile = event.entity
        val shooter = (projectile as? EntityProjectile)?.shootingEntity

        if (projectile !is EntityArrow || shooter !is Player) return

        val player = shooter
        val lang = Redis.getPlayerLang(player.name)
        val now = System.currentTimeMillis()
        val lastUsed = teleportCooldown[player.name] ?: 0

        if (now - lastUsed < 3000) {
            player.sendActionBar(TranslateAPI.get("event.teleport.wait", lang))
            return
        }

        val hitLocation = Location(projectile.x, projectile.y, projectile.z, player.yaw, player.pitch, projectile.level)

        player.teleport(hitLocation)
        player.sendActionBar(TranslateAPI.get("event.teleport.done", lang))

        teleportCooldown[player.name] = now

        TeleportBowTask(player, teleportCooldown).runTaskTimer(Core.instance, 0, 1)
    }

    @EventHandler
    fun onArrowLaunch(event: ProjectileLaunchEvent) {
        val arrow = event.entity
        val shooter = arrow?.shootingEntity as? Player ?: return

        if (arrow !is EntityArrow) return

        ArrowSoundTask(arrow, shooter).runTaskTimer(Core.instance, 0, 2)
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPickup(event: InventoryPickupItemEvent) {
        event.isCancelled = true
    }
}
