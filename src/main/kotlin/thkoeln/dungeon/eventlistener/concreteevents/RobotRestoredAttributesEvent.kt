package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*
import thkoeln.dungeon.domainprimitives.RestorationType

data class RobotRestoredAttributesEvent(
    var robotId: UUID,
    var restorationType: RestorationType,
    var availableEnergy: Int,
    var availableHealth: Int
): AbstractEvent() {

    override val isValid: Boolean
        get() = availableEnergy >= 0 && availableHealth >= 0
}