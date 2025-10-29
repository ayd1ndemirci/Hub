package redfox.hub.commands

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender

class SpawnCommand : Command(
    "spawn",
    "Teleport spawn"
) {

    override fun execute(sender: CommandSender?, commandLabel: String?, args: Array<out String?>?): Boolean {
        if (sender !is Player) return false

        sender.teleport(sender.level.safeSpawn)
        return true
    }
}