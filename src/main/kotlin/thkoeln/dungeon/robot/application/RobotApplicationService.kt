package thkoeln.dungeon.robot.application


import thkoeln.dungeon.strategy.application.StrategyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.*
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.planet.application.PlanetApplicationService
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import java.util.*
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.robot.domain.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.random.Random


/**
 * Service provides functionality on robots
 */

@Service
class RobotApplicationService@Autowired constructor(
    private val robotRepository: RobotRepository,
    private val enemyRobotRepository: EnemyRobotRepository,
    private val planetApplicationService: PlanetApplicationService,
    private val strategyService: StrategyService,
) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)


    fun getTotalNumberOfRobots():Int{
        return robotRepository.count().toInt()
    }

    fun getAllRobots(): List<Robot>{
        return robotRepository.findAll() as MutableList<Robot>
    }

    fun getAllEnemiesOnPlanet(planet:Planet):List<EnemyRobot>{
        return enemyRobotRepository.findAllByPlanetId(planet.planetId)
    }

    fun findRobotByRobotId(robotId: UUID): Robot?{
        val robot = robotRepository.findByRobotId(robotId)
        return if(robot.isPresent)
            robot.get()
        else null
    }


    /*fun createCommandOld(robot: Robot, currentGame: Game?, player: Player?, commandType: CommandType,
                      planetId: UUID?, enemyId: UUID?, itemName: String?, itemQuantity: Int?):Command{

        val commandObject = CommandObject(commandType, planetId, enemyId, itemName, itemQuantity)
        val command = Command(currentGame?.gameId, player?.playerId, robot.robotId, commandType, commandObject)
        return command
    }*/

    fun createCommand(robot: Robot, player: Player, commandType: CommandType, planetId: UUID?,
                      targetId: UUID?, itemName: String, itemQuantity: Int): Command {
        /*val data: CommandData = when (commandType.stringValue) {
            "buying" -> BuyTradableCommandData(robot.robotId, itemName, itemQuantity)
            "selling" -> SellTradablesCommandData(robot.robotId)
            "movement" -> MoveCommandData(robot.robotId, planetId!!)
            "battle" -> BattleCommandData(robot.robotId, targetId!!)
            "mining" -> MineCommandData(robot.robotId)
            "regeneration" -> RegenerateCommandData(robot.robotId)
            else -> return null
        }*/
        val commandObject = CommandObject(robot.robotId,planetId, targetId, itemName, itemQuantity)
        val command = Command(player.playerId!!, commandType.stringValue)
        command.commandObject = commandObject
        return command
    }



    fun getNextUpgradeLevelAsString(robot: Robot, upgradeType: UpgradeType): String{
        return when(upgradeType){
            UpgradeType.HEALTH -> "${upgradeType}_${robot.healthLevel + 1}"
            UpgradeType.DAMAGE -> "${upgradeType}_${robot.damageLevel + 1}"
            UpgradeType.MINING -> "${upgradeType}_${robot.miningLevel + 1}"
            UpgradeType.MINING_SPEED -> "${upgradeType}_${robot.miningSpeedLevel + 1}"
            UpgradeType.MAX_ENERGY -> "${upgradeType}_${robot.energyLevel + 1}"
            UpgradeType.ENERGY_REGEN -> "${upgradeType}_${robot.energyRegenLevel + 1}"
            UpgradeType.STORAGE -> "${upgradeType}_${robot.inventory.storageLevel + 1}"
        }
    }


    //toDO: nimmt viel zeit in anspruch
    fun checkIfXPercentHaveUpgradeTypeLevelOrHigher(level: Int, percentage: Float, upgradeType: UpgradeType):Boolean{
        val robots: MutableList<Robot> = robotRepository.findAll() as MutableList<Robot>
        var robotsWithUpgradeTypeLevel : MutableList<Robot> = ArrayList()

        when(upgradeType){
            UpgradeType.MINING_SPEED -> robotsWithUpgradeTypeLevel = robots.filter { it.miningSpeedLevel >= level }.filter { it.job.isMiner() } as MutableList<Robot>
            UpgradeType.STORAGE -> robotsWithUpgradeTypeLevel = robots.filter { it.inventory.storageLevel >= level }.filter { it.job.isMiner() } as MutableList<Robot>
            UpgradeType.MINING -> robotsWithUpgradeTypeLevel = robots.filter { it.miningLevel >= level }.filter { it.job.isMiner() } as MutableList<Robot>
            UpgradeType.DAMAGE -> robotsWithUpgradeTypeLevel = robots.filter { it.damageLevel >= level }.filter { it.job.isFighter() } as MutableList<Robot>
            UpgradeType.ENERGY_REGEN -> robotsWithUpgradeTypeLevel = robots.filter { it.energyRegenLevel >= level }.filter { it.job == RobotJob.EXPLORER } as MutableList<Robot>
            UpgradeType.HEALTH -> robotsWithUpgradeTypeLevel = robots.filter { it.healthLevel >= level }.filter { it.job.isFighter() } as MutableList<Robot>
            UpgradeType.MAX_ENERGY -> robotsWithUpgradeTypeLevel = robots.filter { it.energyLevel >= level }.filter { it.job == RobotJob.EXPLORER } as MutableList<Robot>
        }

        val listSizeByJob: Int
        if(upgradeType== UpgradeType.MINING_SPEED || upgradeType== UpgradeType.STORAGE || upgradeType== UpgradeType.MINING)
            listSizeByJob = robots.filter { it.job.isMiner() }.size
        else if(upgradeType== UpgradeType.DAMAGE || upgradeType== UpgradeType.HEALTH)
            listSizeByJob = robots.filter { it.job.isFighter() }.size
        else
            listSizeByJob = robots.filter { it.job.isExplorer() }.size
        val percentageRobotCount = listSizeByJob*percentage/100
        return robotsWithUpgradeTypeLevel.size >= percentageRobotCount.toInt()
    }

    fun whichUpgradeTypeNeedsUpgrade(robot: Robot): UpgradeType{
        if(robot.job.isMiner()){
            if(robot.job.minesWhichType()?.neededMiningLevel()!! > robot.miningLevel)
                return UpgradeType.MINING
            else {
                var type: UpgradeType = UpgradeType.MINING
                if(robot.inventory.storageLevel < robot.miningSpeedLevel)
                    type = UpgradeType.STORAGE
                return type
            }
        }
        else if(robot.job.isFighter()){
            var type: UpgradeType = UpgradeType.HEALTH
            if(robot.damageLevel < robot.healthLevel)
                type = UpgradeType.DAMAGE
            return type
        }
        return UpgradeType.ENERGY_REGEN
    }

    //toDO besser strategy überarbeiten, eventuell shortest path finden
    fun findPlanetToMoveTo(robot: Robot): Planet?{
        if(robot.job.isMiner() && robot.job.minesWhichType() == robot.planet._resourceType)
            return null
        val possibleNeighbours = robot.getAllNeighbourPlanets()
        for(neighbour in possibleNeighbours){
            if(robot.job.minesWhichType() == neighbour._resourceType)
                return neighbour
        }
        if(robot.moveHistory.isNotEmpty()) {
            val lastVisitedPlanet = planetApplicationService.findByPlanetId(robot.moveHistory.last())
            if (possibleNeighbours.size == 1)
                return lastVisitedPlanet
            else
                possibleNeighbours.remove(lastVisitedPlanet)
        }
        val unvisitedPossibleNeighbours = possibleNeighbours.filter { !it.hasBeenVisited() }
        if(unvisitedPossibleNeighbours.isNotEmpty()) {
            return unvisitedPossibleNeighbours[Random.nextInt(0, unvisitedPossibleNeighbours.size)]
        }
        else {
            return if (possibleNeighbours.isNotEmpty())
                possibleNeighbours[Random.nextInt(0, possibleNeighbours.size)]
            else
                null
        }
    }

    /*
        Methode soll eigentlich erst benutzt werden, wenn shortest path zu keinem ergebnis führt. Dann soll darauf geachtet
        werden, dass man in der Äußeren Zone bleibt, da dort die Wahrscheinlichkeit für kohle am höchsten ist und es
        sollen unvisited planets bevorzugt werden
     */
    fun findCoalPlanetOrLikeliestToContainCoalToMoveTo(robot: Robot): Planet?{
        if(MineableResourceType.COAL == robot.planet._resourceType)
            return null
        val possibleNeighbours = robot.getAllNeighbourPlanets()
        for(neighbour in possibleNeighbours){
            if(MineableResourceType.COAL== neighbour._resourceType)
                return neighbour
        }
        if(robot.moveHistory.isNotEmpty()) {
            val lastVisitedPlanet = planetApplicationService.findByPlanetId(robot.moveHistory.last())
            if (possibleNeighbours.size == 1)
                return lastVisitedPlanet
            else
                possibleNeighbours.remove(lastVisitedPlanet)
        }
        val unvisitedPossibleNeighbours = possibleNeighbours.filter { !it.hasBeenVisited() }
            .sortedBy{it.movementDifficulty.difficulty}
        if(unvisitedPossibleNeighbours.isNotEmpty()) {
            return unvisitedPossibleNeighbours.first
        }
        else {
            return if (possibleNeighbours.isNotEmpty())
                possibleNeighbours.sortedBy{it.movementDifficulty.difficulty}[0]
            else
                null
        }
    }

    fun moneyNeededForNextUpgrade(robot: Robot, game: Game, upgradeType: UpgradeType): Moneten?{
        val itemName = getNextUpgradeLevelAsString(robot,upgradeType)
        val shopItem = game.shop.filter { it.name == itemName}
        return if(shopItem.isEmpty())
            null
        else
            shopItem[0].price
    }


    fun saveRobot(robot: Robot){
        robotRepository.save(robot)
    }

    fun deleteAllRobots(){
        robotRepository.deleteAll()
    }

    fun getRobotsByJob(job: RobotJob): List<Robot> {
        return robotRepository.findByJob(job)
        //val robots = getAllRobots()
        //return robots.filter { it.job == job }.toMutableList()
    }

    // toDO überarbeiten, nimmt auch viel zeit ein
    fun upgradeRobotsJobs(game: Game){
        val strategy = strategyService.getStrategyByGame(game)
        //Begrenzung, damit es nicht zu viele Job-upgrades gibt, aber keine Roboter mehr die Geld einbringen
        val allRobots = getAllRobots()
        if(strategy.maxNumberOfRobots.toDouble()/allRobots.filter { it.job.isMiner() }.size.toDouble() < 1.25) {

            val coalRobots = robotRepository.findByJob(RobotJob.COAL_WORKER).toMutableList()

            val ironRobots = robotRepository.findByJob(RobotJob.IRON_WORKER).toMutableList()
            var numberOfNewIronRobots = strategy.maxNumberOfIronMiners - ironRobots.size
            if (numberOfNewIronRobots > 0) {
                val possibleIronRobots = coalRobots.filter { it.inventory.usedStorage == 0 }
                if (numberOfNewIronRobots > possibleIronRobots.size)
                    numberOfNewIronRobots = possibleIronRobots.size
                for (i in 0 until numberOfNewIronRobots) {

                    coalRobots.remove(possibleIronRobots[i])
                    possibleIronRobots[i].job = RobotJob.IRON_WORKER
                    saveRobot(possibleIronRobots[i])
                    logger.info("Changed one robot to iron-worker!")
                }

            }

            val gemRobots = robotRepository.findByJob(RobotJob.GEM_WORKER).toMutableList()
            var numberOfNewGemRobots = strategy.maxNumberOfGemMiners - gemRobots.size
            if (numberOfNewGemRobots > 0) {
                val possibleGemRobots = coalRobots.filter { it.inventory.usedStorage == 0 }
                if (numberOfNewGemRobots > possibleGemRobots.size)
                    numberOfNewGemRobots = possibleGemRobots.size
                for (i in 0 until numberOfNewGemRobots) {
                    coalRobots.remove(possibleGemRobots[i])
                    possibleGemRobots[i].job = RobotJob.GEM_WORKER
                    saveRobot(possibleGemRobots[i])
                    logger.info("Changed one robot to gem-worker!")
                }
            }

            val fighterRobots = robotRepository.findByJob(RobotJob.FIGHTER).toMutableList()
            var numberOfNewFighterRobots = strategy.maxNumberOfGemMiners - fighterRobots.size
            if (numberOfNewFighterRobots > 0) {
                val possibleFighterRobots = coalRobots.filter { it.inventory.usedStorage == 0 }
                if (numberOfNewFighterRobots > possibleFighterRobots.size)
                    numberOfNewFighterRobots = possibleFighterRobots.size
                for (i in 0 until numberOfNewFighterRobots) {
                    coalRobots.remove(possibleFighterRobots[i])
                    possibleFighterRobots[i].job = RobotJob.FIGHTER
                    saveRobot(possibleFighterRobots[i])
                    logger.info("Changed one robot to fighter!")
                }
            }
        }
    }

    fun resetEnemyRepository(){
        enemyRobotRepository.deleteAll()
    }

    fun getAllEnemyRobots(): List<EnemyRobot>{
        return enemyRobotRepository.findAll().toList()
    }

    fun removeAllDeadRobots(){
        robotRepository.removeRobotByAlive(false)
        enemyRobotRepository.removeRobotByAlive(false)
    }


}