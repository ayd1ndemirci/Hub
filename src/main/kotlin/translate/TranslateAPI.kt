package translate

object TranslateAPI {
    private val manager = TranslationManager

    @JvmStatic
    fun get(key: String, lang: String, vararg args: Pair<String, String>): String {
        val argsMap = if (args.isEmpty()) emptyMap() else args.toMap()
        return manager.getTranslation(key, lang, argsMap)
    }
}