package thkoeln.dungeon.restadapter

import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.domainprimitives.MineableResource
import thkoeln.dungeon.domainprimitives.MineableResourceType
import thkoeln.dungeon.domainprimitives.MovementDifficulty
import thkoeln.dungeon.planet.domain.Planet
import java.util.*

data class PlanetDto(
    val planetId: UUID,
    val resourceType: MineableResourceType?,
    val movementDifficulty: Int,
    val gameWorldId: UUID,

    var northNeighbour: Planet,

    var eastNeighbour: Planet,

    var southNeighbour: Planet,

    var westNeighbour: Planet,

    var mineableResource: MineableResource,

    @JsonProperty("resourceType")
    var _resourceType: MineableResourceType,

) {
}
