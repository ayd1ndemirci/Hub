package redfox.hub.database

import redis.clients.jedis.JedisPool

object Redis {
    private val jedisPool = JedisPool("localhost", 6379)

    fun getPlayerServer(playerName: String): String? {
        jedisPool.resource.use { jedis ->
            return jedis.get("player:${playerName.lowercase()}:server")
        }
    }

    fun setPlayerServer(playerName: String, serverName: String) {
        jedisPool.resource.use { jedis ->
            jedis.set("player:${playerName.lowercase()}:server", serverName)
        }
    }

    fun deletePlayerServer(playerName: String) {
        jedisPool.resource.use { jedis ->
            jedis.del("player:${playerName.lowercase()}:server")
        }
    }

    fun getSkyblockActivePlayerCount(): Long {
        jedisPool.resource.use { jedis ->
            val count = jedis.get("active_players")
            return count?.toLongOrNull() ?: 0L
        }
    }

    fun getPlayerLang(playerName: String): String {
        jedisPool.resource.use { jedis ->
            return jedis.get("player:${playerName.lowercase()}:lang") ?: "en_US"
        }
    }

    fun setPlayerLang(playerName: String, lang: String) {
        jedisPool.resource.use { jedis ->
            jedis.set("player:${playerName.lowercase()}:lang", lang)
        }
    }
}
