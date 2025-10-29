package redfox.hub.manager

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import redfox.hub.database.Redis
import translate.TranslateAPI

class ItemsManager {

    private val items = mutableMapOf<Int, Item>()

    fun registerItem(slot: Int, item: Item): ItemsManager {
        items[slot] = item
        return this
    }

    fun registerEnchantedItem(slot: Int, item: Item, vararg enchants: Pair<Int, Int>): ItemsManager {
        for ((id, level) in enchants) {
            val enchant = Enchantment.getEnchantment(id)
            enchant.level = level
            item.addEnchantment(enchant)
        }
        items[slot] = item
        return this
    }

    fun getItem(slot: Int, lang: String): Item? {
        val baseItem = items[slot]?.clone() ?: return null
        val key = when (slot) {
            0 -> "item.cosmetic"
            4 -> "item.play"
            8 -> "item.profile"
            else -> null
        }
        val translatedName = key?.let { TranslateAPI.get(it, lang) } ?: baseItem.customName
        return baseItem.setCustomName(translatedName)
    }

    fun giveItems(player: Player) {
        val lang = Redis.getPlayerLang(player.name)
        for ((slot, _) in items) {
            getItem(slot, lang)?.let { player.inventory.setItem(slot, it) }
        }
    }

    fun clear() {
        items.clear()
    }
}
