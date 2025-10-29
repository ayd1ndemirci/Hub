package redfox.hub.task

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemArrow
import cn.nukkit.level.Sound
import cn.nukkit.scheduler.NukkitRunnable
import redfox.hub.database.Redis
import translate.TranslateAPI

class TeleportBowTask(
    private val player: Player,
    private val teleportCooldown: MutableMap<String, Long>
) : NukkitRunnable() {

    private var timeLeft = 20

    override fun run() {
        if (!player.isOnline) {
            teleportCooldown.remove(player.name)
            this.cancel()
            return
        }

        val lang = Redis.getPlayerLang(player.name)

        if (timeLeft <= 0) {
            player.sendActionBar(TranslateAPI.get("teleport_bow.ready", lang))
            player.level.addSound(player, Sound.NOTE_PLING, 1f, 1f, player)
            player.inventory.contents.forEach { slot, item ->
                if (item is ItemArrow) {
                    player.inventory.setItem(slot, Item.get("air"))
                }
            }

            player.inventory.setItem(27, Item.get(Item.ARROW, 0, 1))
            teleportCooldown.remove(player.name)
            this.cancel()
            return
        }

        val redBars = timeLeft
        val grayBars = 20 - timeLeft

        val progress = buildString {
            repeat(redBars) { append("§c|") }
            repeat(grayBars) { append("§7|") }
        }

        val waitMessage = TranslateAPI.get("teleport_bow.waiting", lang, "progress" to progress)
        player.sendActionBar(waitMessage)

        player.inventory.contents.forEach { slot, item ->
            if (item is ItemArrow) {
                player.inventory.setItem(slot, Item.get("air"))
            }
        }
        timeLeft--
    }
}
