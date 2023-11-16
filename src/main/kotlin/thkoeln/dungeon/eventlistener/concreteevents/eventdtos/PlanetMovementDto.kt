package thkoeln.dungeon.eventlistener.concreteevents.eventdtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class PlanetMovementDto(
    @JsonProperty("id")
    var planetId : UUID,
    var movementDifficulty: Int
) {
}