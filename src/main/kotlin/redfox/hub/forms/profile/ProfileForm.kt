package redfox.hub.forms.profile

import cn.nukkit.Player
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.custom.ElementDropdown
import cn.nukkit.form.element.custom.ElementInput
import cn.nukkit.form.element.simple.ElementButton
import cn.nukkit.form.window.CustomForm
import cn.nukkit.form.window.SimpleForm
import redfox.hub.database.Database
import redfox.hub.database.Redis
import redfox.hub.model.ItemStorage
import translate.TranslateAPI

object ProfileForm {

    private val supportedLanguages = listOf(
        "tr_TR" to "Türkçe",
        "en_US" to "English"
    )

    fun send(player: Player, targetName: String = player.name) {
        val lang = Redis.getPlayerLang(player.name)

        val playerData = Database.getPlayerData(targetName)

        val server = Redis.getPlayerServer(targetName) ?: TranslateAPI.get("offline", lang)
        val afk = if (playerData?.afk == true)
            TranslateAPI.get("yes", lang) else TranslateAPI.get("no", lang)
        val tag = playerData?.tag ?: TranslateAPI.get("player", lang)
        val islandLevel = playerData?.islandLevel ?: 0
        val friendCount = playerData?.friendCount ?: 0
        val language = playerData?.language ?: supportedLanguages.find { it.first == lang }?.second ?: lang
        val firstJoin = playerData?.firstJoinFormatted ?: TranslateAPI.get("unknown", lang)
        val badges = playerData?.badgesCount ?: 0

        val statusText = if (server == TranslateAPI.get("offline", lang)) {
            "§c$server"
        } else {
            "§a" + TranslateAPI.get("online", lang) + " §2($server)"
        }

        val form = SimpleForm(TranslateAPI.get("profile.title", lang))  // "Profil"

        form.addElement(ElementLabel("§7${TranslateAPI.get("name", lang)}: §b$targetName"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("status", lang)}: $statusText"))
        form.addElement(ElementLabel("§7AFK: $afk"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("tag", lang)}: $tag"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("island_level", lang)}: §b$islandLevel ${TranslateAPI.get("level", lang)}"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("friend_count", lang)}: §b$friendCount"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("language", lang)}: §b$language"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("first_join", lang)}: §b$firstJoin"))
        form.addElement(ElementLabel("§7${TranslateAPI.get("badges", lang)}: §b$badges/12"))

        if (player.name.equals(targetName, ignoreCase = true)) {
            form.addElement(ElementButton(TranslateAPI.get("view_other_profile", lang)))
            form.addElement(ElementButton(TranslateAPI.get("change_language", lang)))
        }

        form.onSubmit { _, response ->
            if (response == null) return@onSubmit
            when (response.button().text()) {
                TranslateAPI.get("view_other_profile", lang) -> showTargetInput(player)
                TranslateAPI.get("change_language", lang) -> showLanguageSelection(player)
            }
        }

        form.send(player)
    }


    private fun showTargetInput(player: Player, message: String = "") {
        val lang = Redis.getPlayerLang(player.name)

        val form = CustomForm(TranslateAPI.get("profile.view_title", lang))  // "Profil Bak"
        form.addElement(ElementInput(
            if (message.isEmpty()) "" else message,
            TranslateAPI.get("profile.input_placeholder", lang) // "Örn: MesquiNn"
        ))

        form.onSubmit { _, response ->
            val name = response.getInputResponse(0).trim()
            if (name.isEmpty()) {
                showTargetInput(player, "§c" + TranslateAPI.get("profile.error.empty_name", lang)) // "Oyuncu adı boş olamaz."
            } else {
                val playerData = Database.getPlayerData(name)
                if (playerData == null) {
                    showTargetInput(player, "§c" + TranslateAPI.get("profile.error.not_found", lang, "name" to name)) // "Oyuncu bulunamadı: $name"
                } else {
                    send(player, name)
                }
            }
        }
        form.send(player)
    }

    private fun showLanguageSelection(player: Player) {
        val lang = Redis.getPlayerLang(player.name)
        val form = CustomForm(TranslateAPI.get("language_selection.title", lang))

        form.addElement(ElementDropdown(TranslateAPI.get("language_selection.dropdown_label", lang), supportedLanguages.map { it.second }))

        form.onSubmit { _, response ->
            if (response == null) return@onSubmit
            val selectedIndex = response.getDropdownResponse(0).elementId()
            if (selectedIndex in supportedLanguages.indices) {
                val (langCode, langName) = supportedLanguages[selectedIndex]
                Redis.setPlayerLang(player.name, langCode)
                player.sendMessage(TranslateAPI.get("language_selection.changed", langCode, "langName" to langName))

                player.inventory.clearAll()
                ItemStorage.manager.giveItems(player)
            }
        }
        form.send(player)
    }


}
