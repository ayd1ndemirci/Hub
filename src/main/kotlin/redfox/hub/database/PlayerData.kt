package redfox.hub.database

import org.bson.Document

data class PlayerData(
    val name: String,
    val firstJoinFormatted: String?,
    val badgesCount: Int,
    val friendCount: Int,
    val afk: Boolean,
    val tag: String,
    val islandLevel: Int,
    val language: String
) {
    companion object {
        fun fromDocument(doc: Document): PlayerData {
            val name = doc.getString("name") ?: "Unknown"
            val firstJoin = doc.getString("firstJoinFormatted")
            val badges = doc.getList("badges", String::class.java) ?: emptyList()
            val friends = doc.getList("friends", String::class.java) ?: emptyList()
            val afk = doc.getBoolean("afk", false)
            val tag = doc.getString("tag") ?: "Oyuncu"
            val islandLevel = doc.getInteger("islandLevel", 0)
            val language = doc.getString("language") ?: "English"
            return PlayerData(
                name,
                firstJoin,
                badges.size,
                friends.size,
                afk,
                tag,
                islandLevel,
                language
            )
        }
    }
}
