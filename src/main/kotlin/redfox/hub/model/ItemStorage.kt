package redfox.hub.model

import cn.nukkit.block.BlockID
import cn.nukkit.item.Item
import cn.nukkit.item.ItemID
import redfox.hub.manager.ItemsManager

object ItemStorage {
    val manager = ItemsManager().apply {
        registerItem(0, Item.get(BlockID.ENDER_CHEST))
        registerItem(4, Item.get(ItemID.COMPASS))
        registerItem(8, Item.get(ItemID.MAGMA_CREAM))
    }
}

