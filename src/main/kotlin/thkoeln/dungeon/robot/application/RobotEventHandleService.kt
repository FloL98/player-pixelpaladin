package thkoeln.dungeon.robot.application


import com.fasterxml.jackson.databind.ObjectMapper
import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.MovementDifficulty
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.eventlistener.concreteevents.*
import thkoeln.dungeon.planet.application.PlanetApplicationService
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.planet.domain.PlanetDomainService
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.*
import java.util.*


/**
 * Service handles incoming events
 */

@Service
class RobotEventHandleService @Autowired constructor(
    private val robotRepository: RobotRepository,
    private val enemyRobotRepository: EnemyRobotRepository,
    private val planetApplicationService: PlanetApplicationService,
    private val planetDomainService: PlanetDomainService,
) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)
    private var modelMapper = ModelMapper()



    fun handleRobotRevealedIntegrationEvent(robotsRevealedEvent: RobotsRevealedEvent){
        val revealedRobots = robotsRevealedEvent.robots//.map { EnemyRobot().createFromRevealedRobotDto(it) }
        val robotIds = robotRepository.findAll().map{it.robotId}
        val enemyRobotList= revealedRobots.filter{it.robotId !in robotIds}.map { EnemyRobot().createFromRevealedRobotDto(it) }
        enemyRobotRepository.saveAll(enemyRobotList)
        //val robotsToDelete = enemyRobotRepository.findAll().filter { it !in enemyRobotList }
        //enemyRobotRepository.deleteAll(robotsToDelete)

        //experimentell (not actually sure ob Befehl 100% funktioniert)
        enemyRobotRepository.deleteAllNotInList(enemyRobotList)
        logger.info("Enemy robot count: ${enemyRobotRepository.count()}")
    }






    fun handleRobotHealthUpdatedEvent(robotHealthUpdatedEvent: RobotHealthUpdatedEvent){
        changeRobotHealthToAmount(robotHealthUpdatedEvent.robotId!!, robotHealthUpdatedEvent.health!!)
    }

    private fun changeRobotHealthToAmount(robotId: UUID, healthAmount: Int){
        val robot = robotRepository.findByRobotId(robotId)
            .orElseThrow { RobotApplicationException("Couldnt change robot health because robot doesnt exist") }
        robot.health = healthAmount
        robotRepository.save(robot)
        logger.info("Robot $robot successfully changed his energy to $healthAmount!")
    }


    fun handleRobotMovedIntegrationEvent(robotMovedEvent: RobotMovedEvent){
        val robot = robotRepository.findByRobotId(robotMovedEvent.robotId!!)
            .orElseThrow {RobotApplicationException("Couldnt change robot position because robot doesnt exist")}
        var newPlanetPosition = planetApplicationService.findByPlanetId(robotMovedEvent.toPlanet?.planetId!! )
        robot.planet = newPlanetPosition
        robot.energy = robotMovedEvent.remainingEnergy!!
        planetDomainService.visitPlanet(robotMovedEvent.toPlanet?.planetId!!)
        robot.moveHistory.add(robotMovedEvent.fromPlanet?.planetId!!)
        robotRepository.save(robot)
        //logger.info("Robot $robot successfully changed Position to ${newPlanetPosition.planetId} and has now ${robot.energy} energy!")
    }


    //toDO() renew this method maybe?
    fun handleRobotAttackedIntegrationEvent(robotAttackedEvent: RobotAttackedEvent){
        val attackerOptional =  robotRepository.findByRobotId(robotAttackedEvent.attacker?.robotId!!)
        val targetOptional =  robotRepository.findByRobotId(robotAttackedEvent.target?.robotId!!)
        if(attackerOptional.isPresent){
            val attacker = attackerOptional.get()
            if(robotAttackedEvent.attacker?.alive!!){
                attacker.health = robotAttackedEvent.attacker?.availableHealth!!
                attacker.energy = robotAttackedEvent.attacker?.availableEnergy!!
            }
            else {
                robotRepository.removeRobotByRobotId(attacker.robotId)
                logger.info("Removed robot because it died.")
            }
        }
        if(targetOptional.isPresent){
            val target = targetOptional.get()
            if(robotAttackedEvent.target?.alive!!){
                target.health = robotAttackedEvent.target?.availableHealth!!
                target.energy = robotAttackedEvent.target?.availableEnergy!!
            }
            else {
                robotRepository.removeRobotByRobotId(target.robotId)
                logger.info("Removed robot because it died.")
            }
        }
    }
    fun handleRobotUpgradedIntegrationEvent(robotUpgradedEvent: RobotUpgradedEvent){
        var robot = robotRepository.findByRobotId(robotUpgradedEvent.robotId!!)
            .orElseThrow { RobotApplicationException("Couldnt upgrade robot because robot doesnt exist") }
        /*hier eventuell bessere Lösung möglich als jedes Attribut einzeln abzufragen?
        oder einfach  modelMapper.map(robotUpgradedIntegrationEvent.robot, robot) //außer id
        needs to be fixed*/
        when(robotUpgradedEvent.upgradeType){
            UpgradeType.STORAGE -> {robot.inventory?.storageLevel = robotUpgradedEvent.level!!
                robot.inventory?.maxStorage = robotUpgradedEvent.robot?.inventory?.maxStorage!!
            }
            UpgradeType.HEALTH -> {
                robot.healthLevel = robotUpgradedEvent.level!!
                robot.health = robotUpgradedEvent.robot?.health!!
            }
            UpgradeType.DAMAGE -> {
                robot.damageLevel = robotUpgradedEvent.level!!
                robot.attackDamage = robotUpgradedEvent.robot?.attackDamage!!
            }
            UpgradeType.MINING_SPEED -> {
                robot.miningSpeedLevel = robotUpgradedEvent.level!!
                robot.miningSpeed = robotUpgradedEvent.robot?.miningSpeed!!
            }
            UpgradeType.MINING -> {
                robot.miningLevel = robotUpgradedEvent.level!!
            }
            UpgradeType.MAX_ENERGY -> {
                robot.energyLevel = robotUpgradedEvent.level!!
                robot.maxEnergy = robotUpgradedEvent.robot?.maxEnergy!!
            }
            UpgradeType.ENERGY_REGEN -> {
                robot.energyRegenLevel = robotUpgradedEvent.level!!
                robot.energyRegen = robotUpgradedEvent.robot?.energyRegen!!
            }
            else -> {
                logger.info("Couldnt find a suitable type to upgrade robot!")
            }
        }
        robotRepository.save(robot)
        logger.info("Upgraded robots ${robotUpgradedEvent.upgradeType} to level ${robotUpgradedEvent.level}!" )
    }
    fun handleRobotRegeneratedIntegrationEvent(robotRegeneratedEvent: RobotRegeneratedEvent){
        val robot = robotRepository.findByRobotId(robotRegeneratedEvent.robotId!!)
            .orElseThrow {RobotApplicationException("Couldnt change robot energy because robot doesnt exist")}
        robot.energy = robotRegeneratedEvent.availableEnergy!!
        robotRepository.save(robot)
    }
    fun handleRobotResourceMinedIntegrationEvent(robotResourceMinedEvent: RobotResourceMinedEvent){
        val robot = robotRepository.findByRobotId(robotResourceMinedEvent.robotId!!)
            .orElseThrow{ RobotApplicationException("Couldnt add to robot inventory because robot doesnt exist!") }
        robot.inventory.resources = robotResourceMinedEvent.resourceInventory!!
        robotRepository.save(robot)
    }
    fun handleRobotResourceRemovedIntegrationEvent(robotResourceRemovedEvent: RobotResourceRemovedEvent){
        val robot = robotRepository.findByRobotId(robotResourceRemovedEvent.robotId!!)
            .orElseThrow{ RobotApplicationException("Couldnt remove from robot inventory because robot doesnt exist!") }
        robot.inventory?.resources = robotResourceRemovedEvent.resourceInventory!!
        robotRepository.save(robot)
        logger.info("change resources ${robotResourceRemovedEvent.removedResource}")
    }
    fun handleRobotRestoredAttributesIntegrationEvent(robotRestoredAttributesEvent: RobotRestoredAttributesEvent){
        val robot = robotRepository.findByRobotId(robotRestoredAttributesEvent.robotId!!)
            .orElseThrow {RobotApplicationException("Couldnt restore robot attribute because robot doesnt exist")}
        robot.energy = robotRestoredAttributesEvent.availableEnergy!!
        robot.health = robotRestoredAttributesEvent.availableHealth!!
        robotRepository.save(robot)
        logger.info("Robot $robot successfully restored his energy to ${robotRestoredAttributesEvent.availableEnergy}! or health to ${robotRestoredAttributesEvent.availableHealth}")
    }
    fun handleRobotSpawnedIntegrationEvent(robotSpawnedEvent: RobotSpawnedEvent){
        val mapper = ObjectMapper()
        val planet = mapper.convertValue(robotSpawnedEvent.robotDto.planetShortDto, Planet::class.java)
        //val planet = Planet.fromDto(robotSpawnedIntegrationEvent.robotDto.planetShortDto)//robotSpawnedIntegrationEvent.robotDto.planetDto.convertToPlanet() /*Planet()
        planet.movementDifficulty = MovementDifficulty.fromInteger(robotSpawnedEvent.robotDto.planetShortDto.movementDifficulty)
        //modelMapper.map(robotSpawnedIntegrationEvent.robot.planet, planet)
        planetApplicationService.registerPlanetIfNotExists(planet)


        val newRobot= mapper.convertValue(robotSpawnedEvent.robotDto, Robot::class.java)


        //val newRobot1 = Robot()
        //mapper.map(robotSpawnedIntegrationEvent.robotDto, newRobot)
        newRobot.planet = planetApplicationService.findByPlanetId(robotSpawnedEvent.robotDto.planetShortDto.planetId)
        robotRepository.save(newRobot)
        logger.info("Robot $newRobot successfully registered!")
    }

}