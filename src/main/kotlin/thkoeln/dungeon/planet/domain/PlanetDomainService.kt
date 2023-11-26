package thkoeln.dungeon.planet.domain



import java.util.UUID

import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.domainprimitives.MovementDifficulty
import thkoeln.dungeon.domainprimitives.CompassDirection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.PlanetNeighbour
import java.lang.reflect.InvocationTargetException

/**
 * This service primarily aims at making sure that new planets our player learns about are properly connected to
 * each other, i.e. that a real interconnected map is created.
 */
@Service
class PlanetDomainService @Autowired constructor(private val planetRepository: PlanetRepository) {


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