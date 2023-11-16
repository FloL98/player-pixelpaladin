package thkoeln.dungeon.eventlistener.concreteevents.eventdtos

import java.util.*

data class RobotFightResultDto(
    var robotId : UUID,
    var availableHealth: Int,
    var availableEnergy: Int,
    var alive : Boolean = false
) {
}