package thkoeln.dungeon.domainprimitives

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Domain Primitive to represent the type of round status
 */
enum class RoundStatusType(var stringValue: String) {
    @JsonProperty("started")
    STARTED("started"),
    @JsonProperty("ended")
    ENDED("ended"),
    @JsonProperty("command input ended")
    COMMAND_INPUT_ENDED("command input ended");



}