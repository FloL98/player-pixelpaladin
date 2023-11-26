package thkoeln.dungeon.planet.domain



import org.springframework.data.repository.CrudRepository
import java.util.*


interface PlanetRepository : CrudRepository<Planet, UUID?> {
    override fun findAll(): List<Planet>
    //fun findByPlanetId(planetId: UUID?): Optional<Planet>
    //fun findByIsSpaceStationEquals(spaceStationFlag: Boolean?): List<Planet?>?

}