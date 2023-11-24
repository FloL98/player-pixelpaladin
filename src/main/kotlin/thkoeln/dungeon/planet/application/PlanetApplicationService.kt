package thkoeln.dungeon.planet.application



import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.modelmapper.ModelMapper
import thkoeln.dungeon.domainprimitives.Coordinate.Companion.initialCoordinate
import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.domainprimitives.TwoDimDynamicArray
import thkoeln.dungeon.planet.domain.PlanetRepository
import thkoeln.dungeon.planet.domain.PlanetDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import thkoeln.dungeon.EntityLockService
import thkoeln.dungeon.eventlistener.concreteevents.PlanetDiscoveredEvent
import thkoeln.dungeon.eventlistener.concreteevents.ResourceMinedEvent
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

@Service
class PlanetApplicationService @Autowired constructor(
    private val planetRepository: PlanetRepository,
    private val planetDomainService: PlanetDomainService,
    private val entityLockService: EntityLockService,
) {
    private val logger = LoggerFactory.getLogger(PlanetApplicationService::class.java)
    var modelMapper = ModelMapper()


    suspend fun handlePlanetDiscoveredEvent(planetDiscoveredEvent: PlanetDiscoveredEvent){
        //val planetMutex = entityLockService.planetLocks.computeIfAbsent(planetDiscoveredEvent.planetId) { Mutex() }
        //planetMutex.withLock {

        val planetIdsToLock = mutableListOf(planetDiscoveredEvent.planetId)
        for(planetNeighbor in planetDiscoveredEvent.neighbours)
            planetIdsToLock.add(planetNeighbor.id)


        while (true) {
            // Versuche, alle Sperren zu erlangen
            val planetIdsWhichsLockIsAquired: MutableList<UUID> = mutableListOf()
            val allLocksAcquired = planetIdsToLock
                .map { val locked =entityLockService.planetLocks.computeIfAbsent(it) { Mutex() }.tryLock()
                if(locked) planetIdsWhichsLockIsAquired.add(it)
                locked}
                .all { it }

            if (allLocksAcquired) {
                try {
                    // Führe die Verarbeitungslogik hier aus
                    val planetOpt = planetRepository.findByPlanetId(planetDiscoveredEvent.planetId)
                    val planet: Planet
                    if (planetOpt.isEmpty)
                        planet = Planet.fromPlanetIdAndMovementDifficultyAndResource(
                            planetDiscoveredEvent.planetId,
                            planetDiscoveredEvent.movementDifficulty,
                            planetDiscoveredEvent.mineableResource
                        )
                    else {
                        planet = planetOpt.get()
                        planet.mineableResource = planetDiscoveredEvent.mineableResource
                    }

                    if (!planet.visited) {
                        planet.visited = true
                        planetRepository.save(planet)
                        for (neighbour in planetDiscoveredEvent.neighbours) {
                            //val neighborMutex = entityLockService.planetLocks.computeIfAbsent(neighbour.id) { Mutex() }
                            //neighborMutex.withLock {
                            planetDomainService.addNeighbourToPlanet(planet, neighbour.id, neighbour.direction)
                            //}
                        }
                    } else
                        planetRepository.save(planet)

                } finally {
                    // Entsperre alle Planeten
                    planetIdsToLock.forEach { entityLockService.planetLocks.computeIfAbsent(it) { Mutex() }.unlock() }
                }
                break // Verlassen der Schleife nach erfolgreicher Verarbeitung
            } else {
                // Nicht alle Sperren konnten erlangt werden, also entsperre alle und versuche es erneut
                planetIdsWhichsLockIsAquired.forEach { entityLockService.planetLocks.computeIfAbsent(it) { Mutex() }.unlock() }
                // Warten, um Deadlocks zu vermeiden
                delay(Random.nextInt(10, 100).toLong())
            }
        }


        //}


        /*val planet = findByPlanetId(planetDiscoveredEvent.planetId)
        modelMapper.map(planetDiscoveredEvent,planet)
        if(!planet.visited) {
            planet.visited = true
            planetRepository.save(planet)
            for (neighbour in planetDiscoveredEvent.neighbours) {
                planetDomainService.addNeighbourToPlanet(planet, neighbour.id, neighbour.direction)
            }
        }
        else
            planetRepository.save(planet)*/

        //only for shortest path testing purpose
        /*val pathCoal = ShortestPathCalculator(findAll()).shortestPathToString(planet,MineableResourceType.COAL)
        val pathIron = ShortestPathCalculator(findAll()).shortestPathToString(planet,MineableResourceType.IRON)
        logger.info("Shortest path COAL: $pathCoal")
        logger.info("Shortest path IRON: $pathIron")*/
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

    fun findByPlanetIdOpt(planetId: UUID):Optional<Planet>{
        return planetRepository.findByPlanetId(planetId)
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

    suspend fun handleResourceMinedEvent(resourceMinedEvent: ResourceMinedEvent){
        val mutex = entityLockService.planetLocks.computeIfAbsent(resourceMinedEvent.planetId) { Mutex() }
        mutex.withLock {
            val planetOpt =
                planetRepository.findByPlanetId(resourceMinedEvent.planetId)//findByPlanetId(resourceMinedEvent.planetId)
            if (planetOpt.isPresent) {
                val planet = planetOpt.get()
                planet.mineableResource = planet.mineableResource?.decreaseBy(resourceMinedEvent.minedAmount) //resourceMinedEvent.resource
                planetRepository.save(planet)
            }
        }
    }

    fun deleteAll(){
        planetRepository.deleteAll()
    }

    fun findAll(): List<Planet>{
        return planetRepository.findAll()
    }
}