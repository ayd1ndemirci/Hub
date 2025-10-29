package redfox.hub.forms.games

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ElementButton
import cn.nukkit.form.window.SimpleForm
import redfox.hub.database.Redis
import redfox.hub.manager.ServerManager
import redfox.hub.transfer.Transfer
import translate.TranslateAPI

object LobbiesForm {

    fun send(player: Player) {
        val lang = Redis.getPlayerLang(player.name)
        val currentLobby = player.level.name
        val formTitle = TranslateAPI.get("lobbies.title", lang)
        val youAreHereLabel = TranslateAPI.get("lobbies.you_are_here", lang)

        val form = SimpleForm(formTitle)

        ServerManager.lobbiesFolderName.forEach { lobbyName ->
            val buttonText = if (lobbyName == currentLobby) "$lobbyName\n§2» $youAreHereLabel «" else lobbyName
            form.addElement(ElementButton(buttonText))
        }

        form.send(player)

        form.onSubmit { _, response ->
            if (response == null) return@onSubmit

            val selectedLobby = ServerManager.lobbiesFolderName.getOrNull(response.buttonId())
            if (selectedLobby != null) {
                Transfer.spawnLobby(player, selectedLobby)
            } else {
                player.sendMessage(TranslateAPI.get("lobbies.invalid_selection", lang))
            }
        }
    }
}
