package thkoeln.dungeon.eventlistener.concreteevents

import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class RobotHealthUpdatedEvent(
    @JsonProperty("robot")
    var robotId: UUID,
    var amount: Int,
    var health: Int
): AbstractEvent() {

    override val isValid: Boolean
        get() = health >=0 && amount != 0
}