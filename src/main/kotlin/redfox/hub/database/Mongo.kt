package redfox.hub.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

object Mongo {
    val client: MongoClient = MongoClients.create("mongodb://localhost:27017")
    val db: MongoDatabase = client.getDatabase("redfox")
}
