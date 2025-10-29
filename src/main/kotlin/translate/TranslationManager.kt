package translate

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

object TranslationManager {
    private val fallbackLang = "en_US"
    private val gson = Gson()
    private val translations = ConcurrentHashMap<String, Map<String, String>>()

    fun loadLocales(localesFolderPath: String) {
        val folder = File(localesFolderPath)
        println("[TranslationManager] localesPath = $localesFolderPath")
        println("[TranslationManager] Folder exists: ${folder.exists()}, isDirectory: ${folder.isDirectory}")
        if (!folder.isDirectory) return

        val files = folder.listFiles { file -> file.extension.equals("json", ignoreCase = true) }
        println("[TranslationManager] JSON dosyaları sayısı: ${files?.size ?: 0}")

        files?.forEach { file ->
            try {
                val langCode = file.nameWithoutExtension
                val content = file.readText(StandardCharsets.UTF_8)
                val json = gson.fromJson(content, JsonObject::class.java)
                val flatMap = flattenJson(json)
                translations[langCode] = flatMap
                println("[TranslationManager] Yüklendi: $langCode (${flatMap.size} anahtar)")
            } catch (ex: Exception) {
                System.err.println("[TranslationManager] '${file.name}' yüklenemedi: ${ex.localizedMessage}")
            }
        }
    }

    private fun flattenJson(json: JsonObject, prefix: String = ""): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((key, value) in json.entrySet()) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            when {
                value.isJsonObject -> result.putAll(flattenJson(value.asJsonObject, fullKey))
                value.isJsonPrimitive -> result[fullKey] = value.asString
            }
        }
        return result
    }

    fun getTranslation(key: String, lang: String, args: Map<String, String> = emptyMap()): String {
        val template = translations[lang]?.get(key)
            ?: translations[fallbackLang]?.get(key)
            ?: return key
        return replacePlaceholders(template, args)
    }

    private fun replacePlaceholders(template: String, args: Map<String, String>): String {
        if (args.isEmpty()) return template

        val sb = StringBuilder(template.length + 16)
        var i = 0
        while (i < template.length) {
            val c = template[i]
            if (c == '{') {
                val end = template.indexOf('}', i + 1)
                if (end > i + 1) {
                    val placeholder = template.substring(i + 1, end)
                    sb.append(args[placeholder] ?: "{$placeholder}")
                    i = end + 1
                    continue
                }
            }
            sb.append(c)
            i++
        }
        return sb.toString()
    }

    fun getAvailableLanguages(): Set<String> = translations.keys

    fun isLoaded(lang: String): Boolean = translations.containsKey(lang)
}
