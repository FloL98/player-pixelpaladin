package thkoeln.dungeon.domainprimitives

import java.util.*

/**
 * Domain Primitive to represent a neighbour of a planet with direction
 */
data class PlanetNeighbour(
    val id: UUID,
    val direction: CompassDirection
) {

}