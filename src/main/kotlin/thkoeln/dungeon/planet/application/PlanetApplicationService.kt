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
import thkoeln.dungeon.planet.domain.PlanetException
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.random.Random

@Service
class PlanetApplicationService @Autowired constructor(
    private val planetRepository: PlanetRepository,
    private val entityLockService: EntityLockService,
) {
    private val logger = LoggerFactory.getLogger(PlanetApplicationService::class.java)


    suspend fun handlePlanetDiscoveredEvent(planetDiscoveredEvent: PlanetDiscoveredEvent) {
        logger.info("planet discovered start")
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
                            logger.warn("add neighbors start")
                            for (neighbour in planetDiscoveredEvent.neighbours) {
                                addNeighbourToPlanet(planet, neighbour.id, neighbour.direction)
                            }
                            logger.warn("add neighbors end")
                        } else
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
                    delay(Random.nextInt(10, 30).toLong())
                    yield()
                }
            }
        }
        logger.info("planet discovered end")
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
    suspend fun handleResourceMinedEvent(resourceMinedEvent: ResourceMinedEvent){
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

    fun deleteAll(){
        planetRepository.deleteAll()
    }

    fun findAll(): List<Planet>{
        return planetRepository.findAll()
    }



    fun addNeighbourToPlanet(planet: Planet, neighbourId: UUID, direction: CompassDirection) {
        val neighbourOpt = planetRepository.findById(neighbourId)
        val neighbour: Planet
        if (neighbourOpt.isPresent)
            neighbour =neighbourOpt.get()
        else {
            neighbour = Planet(neighbourId)
            planetRepository.save(neighbour)
        }
        defineNeighbour(planet,neighbour, direction)

    }

    fun defineNeighbour(planet: Planet, otherPlanet: Planet?, direction: CompassDirection) {
        try {
            val otherGetter = planet.neighbouringGetter(direction.oppositeDirection)
            val setter = planet.neighbouringSetter(direction)
            setter.invoke(planet, otherPlanet)
            val remoteNeighbour = otherGetter.invoke(otherPlanet) as Planet?
            if (planet != remoteNeighbour) {
                val otherSetter = planet.neighbouringSetter(direction.oppositeDirection)
                otherSetter.invoke(otherPlanet, planet)
            }
        } catch (e: IllegalAccessException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        } catch (e: InvocationTargetException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        } catch (e: NoSuchMethodException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        }

        planetRepository.save(planet)
        if(otherPlanet!=null)
            planetRepository.save(otherPlanet)
        closeNeighbouringCycleForAllDirectionsBut(planet,direction)

    }

    fun closeNeighbouringCycleForAllDirectionsBut(planet: Planet, notInThisDirection: CompassDirection)  {
        for (compassDirection in CompassDirection.entries) {
            if (compassDirection == notInThisDirection) continue
            val neighbour = planet.getNeighbour(compassDirection)
            if (neighbour != null) {
                for (ninetyDegrees in compassDirection.ninetyDegrees()) {
                    if (planet.getNeighbour(ninetyDegrees) != null && neighbour.getNeighbour(ninetyDegrees) != null && planet.getNeighbour(
                            ninetyDegrees
                        )!!.getNeighbour(compassDirection) == null
                    ) {
                        defineNeighbour(planet.getNeighbour(ninetyDegrees)!!,
                            neighbour.getNeighbour(ninetyDegrees), compassDirection
                        )
                    }
                }
            }
        }
    }
}