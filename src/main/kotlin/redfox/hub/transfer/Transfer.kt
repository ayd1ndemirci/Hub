package redfox.hub.transfer

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.level.Level
import redfox.hub.database.Redis
import redfox.hub.manager.ServerManager
import translate.TranslateAPI
import java.net.InetSocketAddress

object Transfer {

    fun spawnRandomLobby(player: Player) {
        val lang = Redis.getPlayerLang(player.name)
        val lobbyName = ServerManager.lobbiesFolderName.random()
        getWorld(lobbyName)?.let {
            player.teleport(it.spawnLocation)
            val message = TranslateAPI.get("transfer.current_lobby", lang, "lobbyName" to lobbyName)
            player.sendMessage(message)
        }
    }

    fun spawnLobby(player: Player, lobby: String = "lobby-1") {
        val lang = Redis.getPlayerLang(player.name)
        getWorld(lobby)?.let {
            player.teleport(it.spawnLocation)
            val message = TranslateAPI.get("transfer.current_lobby", lang, "lobbyName" to lobby)
            player.sendMessage(message)
        }
    }

    fun goToSever(player: Player, server: String = "179.61.147.21", port: Int = 19133) {
        val addr = InetSocketAddress(server, port)
        player.transfer(addr)
    }

    fun getWorld(worldName: String = "lobby1"): Level? {
        val server = Server.getInstance()
        if (!server.isLevelLoaded(worldName) && !server.loadLevel(worldName)) return null
        return server.getLevelByName(worldName)
    }
}
