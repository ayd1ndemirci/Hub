package redfox.hub.manager

import cn.nukkit.Server
import cn.nukkit.command.Command
import redfox.hub.Core
import redfox.hub.commands.SpawnCommand
import redfox.hub.config.HubConfig
import redfox.hub.event.Listener
import redfox.hub.task.ControllerTask

object ServerManager {

    lateinit var lobbiesFolderName: List<String>
    lateinit var opsWithIps: Map<String, String>
    val playerIps = mutableMapOf<String, String>()

    fun run() {
        HubConfig.load()

        lobbiesFolderName = HubConfig.lobbies
        opsWithIps = HubConfig.ops

        commands()
        events()
        tasks()
    }

    private fun events() {
        Server.getInstance().pluginManager.registerEvents(Listener(), Core.instance)
    }

    private fun tasks() {
        Server.getInstance().scheduler.scheduleRepeatingTask(ControllerTask(), 20 * 5)
    }

    private fun commands() {
        Server.getInstance().commandMap.clearCommands()
        var total = 0

        fun registerCommand(name: String, command: Command) {
            Server.getInstance().commandMap.register(name, command)
            total++
        }

        registerCommand("spawn", SpawnCommand())
        Server.getInstance().logger.info("Total $total command activated.")
    }
}