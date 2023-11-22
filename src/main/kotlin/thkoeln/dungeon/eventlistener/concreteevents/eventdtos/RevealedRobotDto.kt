package thkoeln.dungeon.eventlistener.concreteevents.eventdtos

import thkoeln.dungeon.domainprimitives.RobotLevels
import java.util.*

data class RevealedRobotDto(
    val robotId: UUID,
    val planetId: UUID,
    val playerNotion: String,
    val health: Int,
    val energy: Int,
    val levels: RobotLevels
){
}