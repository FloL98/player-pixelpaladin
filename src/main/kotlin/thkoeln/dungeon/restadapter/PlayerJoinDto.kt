package thkoeln.dungeon.restadapter


import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
class PlayerJoinDto {
    var gameExchange: String? = null
    var playerQueue: String? = null
}