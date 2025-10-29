package redfox.hub.forms.cosmetic

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ElementButton
import cn.nukkit.form.window.SimpleForm
import redfox.hub.database.Redis
import translate.TranslateAPI

object CosmeticForm {

    fun send(player: Player) {
        val lang = Redis.getPlayerLang(player.name)
        val formTitle = TranslateAPI.get("cosmetic.title", lang)
        val toysButtonText = TranslateAPI.get("cosmetic.buttons.toys", lang)
        // İstersen diğer butonlar da buraya eklenir

        val form = SimpleForm(formTitle)
        form.addElement(ElementButton(toysButtonText))
        //form.addElement(ElementButton(TranslateAPI.get("cosmetic.buttons.capes", lang)))
        //form.addElement(ElementButton(TranslateAPI.get("cosmetic.buttons.costumes", lang)))

        form.send(player)
        form.onSubmit { _, response ->
            if (response == null) return@onSubmit

            when (response.buttonId()) {
                0 -> ToysForm.send(player)
            }
        }
    }
}
