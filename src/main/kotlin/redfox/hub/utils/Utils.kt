package redfox.hub.utils

import cn.nukkit.Player
import kotlin.collections.set

object Utils {

    private val lastWarningTime: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()
    private val formCooldowns = mutableMapOf<String, Long>()
    fun sendWarning(player: Player, message: String) {
        val currentTime = System.currentTimeMillis()
        val warnings = lastWarningTime[player.name]
        if (warnings == null) return
        val lastTime = warnings.getOrDefault(message, 0L)

        if (currentTime - lastTime >= 5000) {
            player.sendMessage(message)
            warnings[message] = currentTime
        }
    }

    fun canOpenForm(player: Player): Boolean {
        val now = System.currentTimeMillis()
        val last = formCooldowns[player.name] ?: 0L

        return if (now - last >= 1000) {
            formCooldowns[player.name] = now
            true
        } else false
    }
}