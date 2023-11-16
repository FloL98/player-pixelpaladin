package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.RobotFightResultDto

data class RobotAttackedEvent(
    var attacker: RobotFightResultDto,
    var target: RobotFightResultDto
): AbstractEvent() {

    override val isValid: Boolean
        get() = true
}