package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.PlanetMovementDto
import java.util.*

data class RobotMovedEvent(
    val robotId: UUID,
    val remainingEnergy: Int,
    val fromPlanet: PlanetMovementDto,
    val toPlanet: PlanetMovementDto,
): AbstractEvent() {

    override val isValid: Boolean
        get() = remainingEnergy >= 0


}