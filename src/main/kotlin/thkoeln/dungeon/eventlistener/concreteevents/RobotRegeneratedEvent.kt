package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class RobotRegeneratedEvent(
    var robotId: UUID,
    var availableEnergy: Int
): AbstractEvent() {

    override val isValid: Boolean
        get() = availableEnergy >= 0
}