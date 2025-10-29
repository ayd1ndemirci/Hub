package redfox.hub

import cn.nukkit.plugin.PluginBase
import redfox.hub.manager.ServerManager
import translate.TranslationManager
import java.util.*

class Core : PluginBase() {

    companion object {
        lateinit var instance: Core
            private set
    }


    override fun onLoad() {
        instance = this
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"))
    }

    override fun onEnable() {
        ServerManager.run()
        val pluginLocalesFolder = this.dataFolder.absolutePath + "/locales"
        TranslationManager.loadLocales(pluginLocalesFolder)
    }
}