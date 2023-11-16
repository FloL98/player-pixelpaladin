package thkoeln.dungeon.planet.application



import org.modelmapper.ModelMapper
import thkoeln.dungeon.domainprimitives.Coordinate.Companion.initialCoordinate
import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.domainprimitives.TwoDimDynamicArray
import thkoeln.dungeon.planet.domain.PlanetRepository
import thkoeln.dungeon.planet.domain.PlanetDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import thkoeln.dungeon.eventlistener.concreteevents.PlanetDiscoveredEvent
import thkoeln.dungeon.eventlistener.concreteevents.ResourceMinedEvent
import java.util.*


@Service
class PlanetApplicationService @Autowired constructor(
    private val planetRepository: PlanetRepository,
    private val planetDomainService: PlanetDomainService
) {
    private val logger = LoggerFactory.getLogger(PlanetApplicationService::class.java)
    var modelMapper = ModelMapper()


    fun updatePlanetAndNeighboursFromEvent(planetDiscoveredEvent: PlanetDiscoveredEvent){
        val planet = findByPlanetId(planetDiscoveredEvent.planetId)
        modelMapper.map(planetDiscoveredEvent,planet)
        if(!planet.visited) {
            planet.visited = true
            planetRepository.save(planet)
            for (neighbour in planetDiscoveredEvent.neighbours) {
                planetDomainService.addNeighbourToPlanet(planet, neighbour.id, neighbour.direction)
            }
        }
        else
            planetRepository.save(planet)
    }


    /**
     * Method to create arrays for display of the planet map
     */
    fun allPlanetsAs2DArrays(): Map<Planet?, TwoDimDynamicArray<Planet?>> {
        val planetMap: MutableMap<Planet?, TwoDimDynamicArray<Planet?>> = HashMap()
        val allPlanets = planetRepository.findAll()
        for (planet in allPlanets) {
            planet.temporaryProcessingFlag = false
            planetRepository.save(planet)
        }
        // create this as a Map of space stations (which are the first planets known to the player) pointing
        // to a local 2d array containing all planets connected to that space station. When two such "islands" are
        // discovered to be connected, one of it is taken out of the map (to avoid printing planets twice)
        val spacestations = planetRepository.findByIsSpaceStationEquals(true)
        for (spacestation in spacestations!!) {
            if (spacestation!!.temporaryProcessingFlag == false) {
                val island : TwoDimDynamicArray<Planet?> = TwoDimDynamicArray(spacestation)
                // not already visited, i.e. this is really an island (= partial graph)
                spacestation.constructLocalIsland(island, initialCoordinate())
                planetMap[spacestation] = island
                planetDomainService.saveAll()
            }
        }
        return planetMap
    }

    fun findByPlanetId(planetId: UUID):Planet{
        return planetRepository.findByPlanetId(planetId).orElseThrow{PlanetApplicationException("Planet doesnt exist!")}
    }

    fun registerPlanetIfNotExists(planet: Planet){
        val planetFromRepo = planetRepository.findByPlanetId(planet.planetId)
        if(planetFromRepo.isEmpty) {
            planetRepository.save(planet)
            logger.info("Planet: $planet registered!")
        }
        else
            logger.info("Planet: $planet already exists!")
    }

    fun updateResourcesOnPlanetByEvent(resourceMinedEvent: ResourceMinedEvent){
        val planet = findByPlanetId(resourceMinedEvent.planetId)
        planet.mineableResource = resourceMinedEvent.resource
        planetRepository.save(planet)
    }

    fun deleteAll(){
        planetRepository.deleteAll()
    }
}