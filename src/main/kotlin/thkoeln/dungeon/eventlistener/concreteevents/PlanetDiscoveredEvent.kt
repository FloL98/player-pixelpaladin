package thkoeln.dungeon.eventlistener.concreteevents

import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.domainprimitives.MineableResource
import thkoeln.dungeon.domainprimitives.PlanetNeighbour
import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class PlanetDiscoveredEvent(
    @JsonProperty("planet")
    val planetId: UUID,
    @JsonProperty("movementDifficulty")
    val movementDifficulty: Int,
    val neighbours: Array<PlanetNeighbour>,
    @JsonProperty("resource")
    val mineableResource: MineableResource?,

): AbstractEvent() {

    override val isValid: Boolean
        get() = movementDifficulty >= 1

    override fun toString(): String {
        var text = ""
        for(neighbour in neighbours) {
            text += " "
            text += neighbour.id.toString()
        }
        return "planetid: $planetId , neighbours: $text , movementdiff: $movementDifficulty"
    }
}