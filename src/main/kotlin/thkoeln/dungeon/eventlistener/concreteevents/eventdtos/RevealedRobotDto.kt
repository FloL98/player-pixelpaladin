package thkoeln.dungeon.eventlistener.concreteevents.eventdtos

import thkoeln.dungeon.domainprimitives.RobotLevels
import java.util.*

data class RevealedRobotDto(
    var robotId: UUID,
    var planetId: UUID,
    var playerNotion: String,
    var health: Int,
    var energy: Int,
    var levels: RobotLevels
){
}