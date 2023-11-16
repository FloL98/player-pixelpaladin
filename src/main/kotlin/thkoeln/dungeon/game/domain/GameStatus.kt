package thkoeln.dungeon.game.domain


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName


enum class GameStatus {
    @JsonProperty("created")
    CREATED,
    @JsonProperty("started")
    STARTED,
    @JsonProperty("ended")
    ENDED;

    val isActive: Boolean
        get() = this == CREATED || this == STARTED
    val isOpenForJoining: Boolean
        get() = this == CREATED
    val isRunning: Boolean
        get() = this == STARTED
}