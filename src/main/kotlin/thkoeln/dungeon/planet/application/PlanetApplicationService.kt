package thkoeln.dungeon.planet.application



import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.planet.domain.PlanetRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import thkoeln.dungeon.EntityLockService
import thkoeln.dungeon.domainprimitives.CompassDirection
import thkoeln.dungeon.eventlistener.concreteevents.PlanetDiscoveredEvent
import thkoeln.dungeon.eventlistener.concreteevents.ResourceMinedEvent
import thkoeln.dungeon.planet.domain.PlanetDomainService
import thkoeln.dungeon.planet.domain.PlanetException
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.random.Random

@Service
class PlanetApplicationService @Autowired constructor(
    private val planetRepository: PlanetRepository,
    private val entityLockService: EntityLockService,
    private val planetDomainService: PlanetDomainService,
) {
    private val logger = LoggerFactory.getLogger(PlanetApplicationService::class.java)


    suspend fun handlePlanetDiscoveredEvent(planetDiscoveredEvent: PlanetDiscoveredEvent) {
        val planetVisited = planetRepository.findById(planetDiscoveredEvent.planetId)
            .map { it.visited }
            .orElse(false)

        if(!planetVisited) {
            while (true) {
                //finds out ids from planet, his neighbors and neighbor neighbors to know what to lock
                val planetIdsToLock = mutableListOf(planetDiscoveredEvent.planetId)
                for (planetNeighbor in planetDiscoveredEvent.neighbours) {
                    val nPlanetOpt = planetRepository.findById(planetNeighbor.id)
                    if (nPlanetOpt.isPresent) {
                        for (nPlanetNeighbors in nPlanetOpt.get().getAllNeighborsAsList()) {
                            if (!planetIdsToLock.contains(nPlanetNeighbors.planetId))
                                planetIdsToLock.add(nPlanetNeighbors.planetId)
                        }
                    }
                    planetIdsToLock.add(planetNeighbor.id)
                }

                //trying to get all locks
                val planetIdsWhichsLockIsAquired: MutableList<UUID> = mutableListOf()
                val allLocksAcquired = planetIdsToLock
                    .map {
                        val locked = entityLockService.planetLocks.computeIfAbsent(it) { Mutex() }.tryLock()
                        if (locked) planetIdsWhichsLockIsAquired.add(it)
                        locked
                    }
                    .all { it }

                if (allLocksAcquired) {
                    try {
                        val planetOpt = planetRepository.findById(planetDiscoveredEvent.planetId)
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
                                planetDomainService.addNeighbourToPlanet(planet, neighbour.id, neighbour.direction)
                            }
                        }
                        else
                            planetRepository.save(planet)

                    } finally {
                        //unlock all locks
                        planetIdsToLock.forEach {
                            entityLockService.planetLocks.computeIfAbsent(it) { Mutex() }.unlock()
                        }
                    }
                    break
                } else {
                    //unlock all locks if couldnt get all locks
                    planetIdsWhichsLockIsAquired.forEach {
                        entityLockService.planetLocks.computeIfAbsent(it) { Mutex() }.unlock()
                    }
                    //wait to void deadlocks
                    delay(Random.nextInt(20, 80).toLong())
                    yield()
                }
            }
        }
    }




    fun findByPlanetId(planetId: UUID):Planet{
        return planetRepository.findById(planetId).orElseThrow{PlanetApplicationException("Planet doesnt exist!")}
    }

    fun findByPlanetIdOpt(planetId: UUID):Optional<Planet>{
        return planetRepository.findById(planetId)
    }

    fun savePlanet(planet: Planet){
        planetRepository.save(planet)
    }

    @Transactional
    suspend fun handleResourceMinedEvent1(resourceMinedEvent: ResourceMinedEvent){
        val mutex = entityLockService.planetLocks.computeIfAbsent(resourceMinedEvent.planetId) { Mutex() }
        mutex.withLock {
            logger.warn("findby planet start")
            val planetOpt =
                planetRepository.findById(resourceMinedEvent.planetId)
            logger.warn("findby planet end")
            if (planetOpt.isPresent) {
                val planet = planetOpt.get()
                planet.mineableResource = planet.mineableResource?.decreaseBy(resourceMinedEvent.minedAmount) //resourceMinedEvent.resource
                logger.warn("save planet start")
                planetRepository.save(planet)
                logger.warn("save planet end")
            }
        }
    }
    @Transactional
    suspend fun handleResourceMinedEvent(resourceMinedEvent: ResourceMinedEvent){
        while(true) {
            val locked = entityLockService.planetLocks.computeIfAbsent(resourceMinedEvent.planetId) { Mutex() }.tryLock()
            if(locked){
                try {
                    val planetOpt =
                        planetRepository.findById(resourceMinedEvent.planetId)
                    if (planetOpt.isPresent) {
                        val planet = planetOpt.get()
                        planet.mineableResource =
                            planet.mineableResource?.decreaseBy(resourceMinedEvent.minedAmount)
                        planetRepository.save(planet)
                    }
                }
                finally{
                    entityLockService.planetLocks.computeIfAbsent(resourceMinedEvent.planetId) { Mutex() }.unlock()
                }
                break
            }
            else
                yield()
        }
    }

    fun deleteAll(){
        planetRepository.deleteAll()
    }

    fun findAll(): List<Planet>{
        return planetRepository.findAll()
    }


}