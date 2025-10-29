package redfox.hub.forms.games

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ElementButton
import cn.nukkit.form.window.SimpleForm
import redfox.hub.database.Redis
import redfox.hub.transfer.Transfer
import translate.TranslateAPI

object GamesForm {

    fun send(player: Player) {
        val lang = Redis.getPlayerLang(player.name)
        val formTitle = TranslateAPI.get("games.title", lang)
        val count = Redis.getSkyblockActivePlayerCount()
        val skyblockButtonText = TranslateAPI.get("games.buttons.skyblock", lang, "count" to count.toString())
        val lobbiesButtonText = TranslateAPI.get("games.buttons.lobbies", lang)

        val form = SimpleForm(formTitle)
        form.addElement(ElementButton(skyblockButtonText))
        form.addElement(ElementButton(lobbiesButtonText))
        form.send(player)
        form.onSubmit { _, response ->
            if (response == null) return@onSubmit
            when (response.buttonId()) {
                0 -> {
                    player.sendMessage(TranslateAPI.get("games.messages.redirecting_skyblock", lang))
                    Transfer.goToSever(player)
                }
                1 -> LobbiesForm.send(player)
            }
        }
    }
}
