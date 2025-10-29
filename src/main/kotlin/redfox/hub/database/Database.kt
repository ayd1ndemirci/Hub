package redfox.hub.database

import com.mongodb.client.model.Filters
import org.bson.Document

object Database {
    private val collection = Mongo.db.getCollection("players")

    fun getPlayerData(name: String): PlayerData? {
        val doc = collection.find(Filters.eq("name", name.lowercase())).firstOrNull()
        return if (doc != null) PlayerData.fromDocument(doc) else null
    }
}
