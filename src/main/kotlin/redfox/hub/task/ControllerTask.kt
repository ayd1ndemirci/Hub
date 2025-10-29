package redfox.hub.task

import cn.nukkit.Server
import cn.nukkit.scheduler.Task
import redfox.hub.database.Redis
import redfox.hub.manager.ServerManager
import translate.TranslateAPI

class ControllerTask : Task() {

    private var lastIndex = 0

    override fun onRun(currentTick: Int) {
        val server = Server.getInstance()
        val players = server.onlinePlayers.values.toList()
        val totalPlayers = players.size
        if (totalPlayers == 0) return

        val checkBatchSize = 1.coerceAtLeast(totalPlayers / 2)

        val endIndex = (lastIndex + checkBatchSize).coerceAtMost(totalPlayers)

        for (i in lastIndex until endIndex) {
            val player = players[i]
            val name = player.name.lowercase()
            val savedIp = ServerManager.playerIps[name] ?: continue
            val currentIp = player.address

            if (savedIp != currentIp) {
                val lang = Redis.getPlayerLang(player.name)
                val msg = TranslateAPI.get("event.kick.ip_changed", lang)
                player.close(msg)
                ServerManager.playerIps.remove(name)
            }

            player.health = 20f
            player.foodData.food = 20
            player.foodData.saturation = 20f
        }

        lastIndex = if (endIndex >= totalPlayers) 0 else endIndex
    }
}
