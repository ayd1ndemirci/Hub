package redfox.hub.model

data class HubSettings(
    var lobbies: MutableList<String> = mutableListOf(),
    var ops: MutableMap<String, String> = mutableMapOf()
)