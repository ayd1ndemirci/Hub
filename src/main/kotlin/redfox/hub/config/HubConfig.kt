package redfox.hub.config

import cn.nukkit.Server
import com.google.gson.Gson
import redfox.hub.model.HubSettings
import java.io.File

object HubConfig {

    private val gson = Gson()

    private val configDir = File(Server.getInstance().dataPath, "settings")
    private val file = File(configDir, "hub.json")

    private var settings = HubSettings()

    fun load() {
        if (!configDir.exists()) configDir.mkdirs()
        if (!file.exists()) saveDefault()

        settings = try {
            gson.fromJson(file.readText(), HubSettings::class.java)
        } catch (e: Exception) {
            saveDefault()
            HubSettings()
        }
    }

    fun save() = file.writeText(gson.toJson(settings))

    private fun saveDefault() {
        settings = HubSettings(
            lobbies = mutableListOf("lobby-1", "lobby-2", "lobby-3", "lobby-4"),
            ops = mutableMapOf("mesquinn" to "1.1.1.1")
        )
        save()
    }

    val lobbies get() = settings.lobbies
    val ops get() = settings.ops
}
