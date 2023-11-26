package thkoeln.dungeon.robot.application


import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import thkoeln.dungeon.EntityLockService
import thkoeln.dungeon.domainprimitives.MovementDifficulty
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.eventlistener.concreteevents.*
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.RobotFightResultDto
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
    private val entityLockService: EntityLockService,
) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)
    private val objectMapper = ObjectMapper()



    fun handleRobotRevealedEvent(robotsRevealedEvent: RobotsRevealedEvent){
        val revealedRobots = robotsRevealedEvent.robots//.map { EnemyRobot().createFromRevealedRobotDto(it) }
        /*val robotIds = robotRepository.findAll().map{it.robotId}
        val enemyRobotList= revealedRobots.filter{it.robotId !in robotIds}.map { EnemyRobot().createFromRevealedRobotDto(it) }*/

        val robotIds = robotRepository.findAll().map { it.robotId }.toSet()
        val enemyRobotList = revealedRobots
            .filter { it.robotId !in robotIds }
            .map { EnemyRobot().createFromRevealedRobotDto(it) }

        if(enemyRobotList.isNotEmpty())
            enemyRobotRepository.saveAll(enemyRobotList)
        //if(enemyRobotList.isEmpty())
        //    enemyRobotRepository.deleteAll()
        enemyRobotRepository.deleteAllNotInList(enemyRobotList)
        logger.info("Enemy robot count: ${enemyRobotRepository.count()}")
    }



    //Wann kommt das Event überhaupt?
    suspend fun handleRobotHealthUpdatedEvent(robotHealthUpdatedEvent: RobotHealthUpdatedEvent){
        val mutex = entityLockService.robotLocks.computeIfAbsent(robotHealthUpdatedEvent.robotId) { Mutex() }
        mutex.withLock {
            val robot = robotRepository.findById(robotHealthUpdatedEvent.robotId).orElseThrow {
                RobotApplicationException("Couldnt change robot health because robot doesnt exist") }
            robot.health += robotHealthUpdatedEvent.amount
            robotRepository.save(robot)
            logger.info("Robot $robot successfully changed his energy to ${robot.health}!")
        }
    }

    private suspend fun changeRobotHealthToAmount(robotId: UUID, healthAmount: Int){


    }


    suspend fun handleRobotMovedEvent(robotMovedEvent: RobotMovedEvent){
        val robotMutex = entityLockService.robotLocks.computeIfAbsent(robotMovedEvent.robotId) { Mutex() }
        robotMutex.withLock {
            val planetMutex = entityLockService.planetLocks.computeIfAbsent(robotMovedEvent.toPlanet.planetId) { Mutex() }
            planetMutex.withLock {
            val robot = robotRepository.findById(robotMovedEvent.robotId).orElseThrow {
                RobotApplicationException("Couldnt change robot position because robot doesnt exist") }
                val newPlanetPosition = planetApplicationService.findByPlanetId(robotMovedEvent.toPlanet.planetId)
                robot.planet = newPlanetPosition
                robot.energy = robotMovedEvent.remainingEnergy
                robot.moveHistory.add(robotMovedEvent.fromPlanet.planetId)
                if(robot.moveHistory.size > 10)
                    robot.moveHistory.removeFirst()
                robotRepository.save(robot)
                logger.info("Robot $robot successfully changed Position to ${newPlanetPosition.planetId} and has now ${robot.energy} energy!")
            }
        }

    }


    suspend fun handleRobotAttackedEvent(robotAttackedEvent: RobotAttackedEvent) {
        fun updateRobot(robot: Robot?, robotFightResultDto: RobotFightResultDto) {
            robot?.apply {
                health = robotFightResultDto.availableHealth
                energy = robotFightResultDto.availableEnergy
                alive = robotFightResultDto.alive
                robotRepository.save(this)
            } ?: enemyRobotRepository.findById(robotFightResultDto.robotId).ifPresent {
                it.health = robotFightResultDto.availableHealth
                it.energy = robotFightResultDto.availableEnergy
                it.alive = robotFightResultDto.alive
                enemyRobotRepository.save(it)
            }
        }
        val mutexAttacker = entityLockService.robotLocks.computeIfAbsent(robotAttackedEvent.attacker.robotId) { Mutex() }
        val mutexTarget = entityLockService.robotLocks.computeIfAbsent(robotAttackedEvent.target.robotId) { Mutex() }
        mutexAttacker.withLock {
            updateRobot(
                robotRepository.findById(robotAttackedEvent.attacker.robotId).orElse(null), robotAttackedEvent.attacker)
        }
        mutexTarget.withLock {
            updateRobot(robotRepository.findById(robotAttackedEvent.target.robotId).orElse(null), robotAttackedEvent.target)
        }
    }



    suspend fun handleRobotUpgradedEvent(robotUpgradedEvent: RobotUpgradedEvent){
        val mutex = entityLockService.robotLocks.computeIfAbsent(robotUpgradedEvent.robotId) { Mutex() }
        mutex.withLock {
            val robot = robotRepository.findById(robotUpgradedEvent.robotId).orElseThrow {
                RobotApplicationException("Couldnt upgrade robot because robot doesnt exist") }
            when (robotUpgradedEvent.upgradeType) {
                UpgradeType.STORAGE -> {
                    robot.upgradeStorage(robotUpgradedEvent.level, robotUpgradedEvent.robot.inventory.maxStorage)
                }
                UpgradeType.HEALTH -> {
                    robot.upgradeHealth(robotUpgradedEvent.level, robotUpgradedEvent.robot.health)
                }
                UpgradeType.DAMAGE -> {
                    robot.upgradeDamage(robotUpgradedEvent.level, robotUpgradedEvent.robot.attackDamage)
                }
                UpgradeType.MINING_SPEED -> {
                    robot.upgradeMiningspeed(robotUpgradedEvent.level, robotUpgradedEvent.robot.miningSpeed)
                }
                UpgradeType.MINING -> {
                    robot.upgradeMining(robotUpgradedEvent.level)
                }
                UpgradeType.MAX_ENERGY -> {
                    robot.upgradeMaxEnergy(robotUpgradedEvent.level, robotUpgradedEvent.robot.maxEnergy)
                }
                UpgradeType.ENERGY_REGEN -> {
                    robot.upgradeEnergyRegen(robotUpgradedEvent.level, robotUpgradedEvent.robot.energyRegen)
                }
            }
            robotRepository.save(robot)
            logger.info("Upgraded robots ${robotUpgradedEvent.upgradeType} to level ${robotUpgradedEvent.level}!")
        }
    }
    suspend fun handleRobotRegeneratedEvent(robotRegeneratedEvent: RobotRegeneratedEvent){
        val mutex = entityLockService.robotLocks.computeIfAbsent(robotRegeneratedEvent.robotId) { Mutex() }
        mutex.withLock {
            val robot = robotRepository.findById(robotRegeneratedEvent.robotId).orElseThrow {
                RobotApplicationException("Couldnt change robot energy because robot doesnt exist")
            }
            robot.energy = robotRegeneratedEvent.availableEnergy
            robotRepository.save(robot)
            logger.info("changed robot energy to ${robot.energy}")
        }
    }

    @Transactional
    suspend fun handleRobotResourceMinedIntegrationEvent(robotResourceMinedEvent: RobotResourceMinedEvent){
        val mutex = entityLockService.robotLocks.computeIfAbsent(robotResourceMinedEvent.robotId) { Mutex() }
        mutex.withLock {
            logger.info("findbyId robot start")
            val robot = robotRepository.findById(robotResourceMinedEvent.robotId)
                .orElseThrow { RobotApplicationException("Couldnt add to robot inventory because robot doesnt exist!") }
            logger.info("findbyId robot end")
            val maxAddedAmount = robot.inventory.maxStorage - robot.inventory.usedStorage
            val addedAmount: Int = minOf(robotResourceMinedEvent.minedAmount, maxAddedAmount)
            robot.inventory.resources = robot.inventory.resources.addFromTypeAndAmount(
                robotResourceMinedEvent.minedResource,
                addedAmount
            )
            logger.info("save() robot start")
            robotRepository.save(robot)
            logger.info("save() robot end")
        }
    }
    suspend fun handleRobotResourceRemovedIntegrationEvent(robotResourceRemovedEvent: RobotResourceRemovedEvent){
        val mutex = entityLockService.robotLocks.computeIfAbsent(robotResourceRemovedEvent.robotId) { Mutex() }
        mutex.withLock {
            val robot = robotRepository.findById(robotResourceRemovedEvent.robotId)
                .orElseThrow { RobotApplicationException("Couldnt remove from robot inventory because robot doesnt exist!") }
            robot.inventory.resources = robot.inventory.resources.removeFromTypeAndAmount(
                robotResourceRemovedEvent.removedResource,
                robotResourceRemovedEvent.removedAmount
            )
            robotRepository.save(robot)
        }
    }
    suspend fun handleRobotRestoredAttributesIntegrationEvent(robotRestoredAttributesEvent: RobotRestoredAttributesEvent){
        val mutex = entityLockService.robotLocks.computeIfAbsent(robotRestoredAttributesEvent.robotId) { Mutex() }
        mutex.withLock {
            val robot = robotRepository.findById(robotRestoredAttributesEvent.robotId)
                .orElseThrow { RobotApplicationException("Couldnt restore robot attribute because robot doesnt exist") }
            robot.energy = robotRestoredAttributesEvent.availableEnergy
            robot.health = robotRestoredAttributesEvent.availableHealth
            robotRepository.save(robot)
        }
    }
    suspend fun handleRobotSpawnedIntegrationEvent(robotSpawnedEvent: RobotSpawnedEvent){

        val planetMutex = entityLockService.planetLocks.computeIfAbsent(robotSpawnedEvent.robotDto.planetShortDto.planetId) { Mutex() }
        planetMutex.withLock {
            val planetOpt =
                planetApplicationService.findByPlanetIdOpt(robotSpawnedEvent.robotDto.planetShortDto.planetId)
            val planet: Planet
            if (planetOpt.isEmpty) {
                planet = objectMapper.convertValue(robotSpawnedEvent.robotDto.planetShortDto, Planet::class.java)
                planet.movementDifficulty =
                    MovementDifficulty.fromInteger(robotSpawnedEvent.robotDto.planetShortDto.movementDifficulty)
                planetApplicationService.savePlanet(planet)
            } else
                planet = planetOpt.get()

            val newRobot = objectMapper.convertValue(robotSpawnedEvent.robotDto, Robot::class.java)
            newRobot.planet = planet

            val robotMutex = entityLockService.robotLocks.computeIfAbsent(robotSpawnedEvent.robotDto.robotId) { Mutex() }
            robotMutex.withLock {
                robotRepository.save(newRobot)
            }
        }
    }

}