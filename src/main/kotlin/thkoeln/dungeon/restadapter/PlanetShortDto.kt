package thkoeln.dungeon.restadapter


import thkoeln.dungeon.domainprimitives.MineableResourceType
import java.util.*

data class PlanetShortDto(
    val planetId: UUID,
    val resourceType: MineableResourceType?,
    val movementDifficulty: Int,
    val gameWorldId: UUID
) {


}