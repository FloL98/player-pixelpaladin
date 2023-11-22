package thkoeln.dungeon.planet.domain



import java.util.UUID

import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.domainprimitives.MovementDifficulty
import thkoeln.dungeon.domainprimitives.CompassDirection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.PlanetNeighbour

/**
 * This service primarily aims at making sure that new planets our player learns about are properly connected to
 * each other, i.e. that a real interconnected map is created.
 * Please make sure not to use the PlanetRepository directly, but use the methods of this service instead.
 */
@Service
class PlanetDomainService @Autowired constructor(private val planetRepository: PlanetRepository) {
    private val logger = LoggerFactory.getLogger(PlanetDomainService::class.java)

    /**
     * Add a new planet (may be space station) we learn about from an external event,
     * without having any information about its neighbours. That could be e.g. when
     * new space stations are declared.
     * @param newPlanetId
     */
    fun addPlanetWithoutNeighbours(newPlanetId: UUID, isSpaceStation: Boolean) {
        var newPlanet: Planet? = null
        val foundPlanets = planetRepository.findAll()
        newPlanet = if (foundPlanets.isEmpty()) {
            // no planets yet. Assign (0,0) to this first one.
            Planet(newPlanetId)
        } else {
            val foundOptional = planetRepository.findByPlanetId(newPlanetId)
            if (foundOptional!!.isPresent) {
                // not sure if this can happen ... but just to make sure, all the same.
                foundOptional.get()
            } else {
                Planet(newPlanetId)
            }
        }
        newPlanet.isSpaceStation = isSpaceStation
        planetRepository.save(newPlanet)
    }

    fun visitPlanetWithDifficulty(planetId: UUID, movementDifficulty: Int) {
        val planet = planetRepository.findByPlanetId(planetId)
            .orElseThrow { PlanetException("Planet with UUID $planetId not found!") }
        planet.visited = true
        planet.movementDifficulty = MovementDifficulty.fromInteger(movementDifficulty)
        planetRepository.save(planet)
    }
    fun visitPlanet(planet: Planet) {
        /*val planet = planetRepository.findByPlanetId(planetId)
            .orElseThrow { PlanetException("Planet with UUID $planetId not found!") }*/
        planet.visited = true
        planetRepository.save(planet)
    }

    //direkt die objeke übergeben ansttatt uuids?
    fun addNeighbourToPlanet(planet: Planet, neighbourId: UUID, direction: CompassDirection) {
        val neighbourOpt = planetRepository.findByPlanetId(neighbourId)
        val neighbour = if (neighbourOpt.isPresent) neighbourOpt.get() else Planet(neighbourId)
        //neighbour.movementDifficulty = MovementDifficulty.fromInteger(movementDifficulty)
        planet.defineNeighbour(neighbour, direction)
        planetRepository.save(planet)
        planetRepository.save(neighbour)
    }

    fun saveAll() {
        val allPlanets = planetRepository.findAll()
        //for (planet in allPlanets) planetRepository.save(planet)
        planetRepository.saveAll(allPlanets)
    }


}