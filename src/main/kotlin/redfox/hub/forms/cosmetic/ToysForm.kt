package redfox.hub.forms.cosmetic

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ElementButton
import cn.nukkit.form.window.SimpleForm
import cn.nukkit.item.Item
import cn.nukkit.item.ItemArrow
import cn.nukkit.item.ItemID
import cn.nukkit.item.enchantment.Enchantment
import redfox.hub.database.Redis
import translate.TranslateAPI

object ToysForm {

    fun send(player: Player) {
        val lang = Redis.getPlayerLang(player.name)

        val formTitle = TranslateAPI.get("cosmetic.toys.title", lang)
        val buttonTeleBow = TranslateAPI.get("cosmetic.toys.buttons.teleport_bow", lang)
        val buttonElytra = TranslateAPI.get("cosmetic.toys.buttons.elytra", lang)
        val buttonBack = TranslateAPI.get("cosmetic.buttons.back", lang)

        val form = SimpleForm(formTitle)
        form.addElement(ElementButton(buttonTeleBow))
        form.addElement(ElementButton(buttonElytra))
        form.addElement(ElementButton(buttonBack))
        form.send(player)
        form.onSubmit { _, response ->
            if (response == null) return@onSubmit

            when (response.buttonId()) {
                0 -> {
                    val bow = Item.get(ItemID.BOW).setCustomName("§r§a$buttonTeleBow")
                    player.inventory.chestplate = Item.get("air")
                    player.inventory.setItem(1, bow)
                    player.inventory.contents.forEach { slot, item ->
                        if (item is ItemArrow) {
                            player.inventory.setItem(slot, Item.get("air"))
                        }
                    }
                    val arrow = Item.get(ItemID.ARROW)
                    player.inventory.setItem(27, arrow)
                    player.sendMessage("§r§8» §a'" + buttonTeleBow + "' " + TranslateAPI.get("cosmetic.toys.messages.added_to_inventory", lang))
                }
                1 -> {
                    val elytra = Item.get(ItemID.ELYTRA).setCustomName("§r§d$buttonElytra")
                    elytra.addEnchantment(Enchantment.get(Enchantment.ID_DURABILITY).setLevel(5))
                    player.inventory.setItem(1, Item.get("air"))
                    player.inventory.setItem(27, Item.get("air"))
                    player.inventory.chestplate = elytra
                    player.sendMessage("§r§8» §a'" + buttonElytra + "' " + TranslateAPI.get("cosmetic.toys.messages.added_to_inventory", lang))
                }
                2 -> CosmeticForm.send(player)
            }
        }
    }
}
