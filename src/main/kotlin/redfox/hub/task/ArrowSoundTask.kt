package redfox.hub.task

import cn.nukkit.Player
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.level.Sound
import cn.nukkit.scheduler.NukkitRunnable

class ArrowSoundTask(
    private val arrow: EntityArrow,
    private val player: Player
) : NukkitRunnable() {
    override fun run() {
        if (arrow.isClosed || arrow.isOnGround) {
            this.cancel()
            arrow.level.addSound(player.position, Sound.MOB_ENDERMEN_PORTAL, 1f, 1f, player)
            return
        }

        val level = arrow.level
        level.addSound(player.position, Sound.RANDOM_CLICK, 1f, 1f, player)
    }
}
