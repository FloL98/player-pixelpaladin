package thkoeln.dungeon.robot.application

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.application.GameApplicationService
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.planet.domain.PlanetDomainService
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.robot.domain.RobotJob
import thkoeln.dungeon.strategy.application.StrategyService

@Service
class TestServiceRobotStrategy@Autowired constructor(
    private val robotApplicationService: RobotApplicationService,
    private val gameServiceRESTAdapter: GameServiceRESTAdapter,
    private val robotEventHandleService: RobotEventHandleService,
    private val strategyService: StrategyService,
    private val planetDomainService: PlanetDomainService,
    private val gameApplicationService: GameApplicationService,
) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)


    //toDo eventuell roboter-flee-strategy?


    fun testRefreshEnergyStratgyFor(robot: Robot, player: Player): Command?{
        if(robot.energy < 3)
            return Command().createRegenerateCommand(player.playerId!!, robot.robotId)
        return null
    }

    fun testFullInventoryStrategyFor(robot: Robot,player: Player): Command?{
        if(robot.inventory.full)
            return Command().createSellingCommand(player.playerId!!,robot.robotId)
        return null
    }

    fun testAttackStrategyFor(robot: Robot, player: Player): Command?{
        val enemyList = robotApplicationService.getAllEnemiesOnPlanet(robot.planet).sortedBy { it.levels.getSumOfFightingLevels() }
        if(enemyList.isNotEmpty())
            return Command().createBattleCommand(player.playerId!!,robot.robotId,enemyList.first.robotId!!)
        else
            return null
    }

    fun testFarmStrategyFor(robot: Robot, player: Player): Command?{
        if (robot.canFarmOnCurrentPlanet() && robot.hasSuitableJobForResource(robot.planet.mineableResource?.resourceType!!))
            return Command().createMiningCommand(player.playerId!!, robot.robotId)
        else
            return null
    }

    fun testMoveStrategyFor(robot: Robot, player: Player): Command?{
        val planetToMoveTo = robotApplicationService.findPlanetToMoveTo(robot)
        if(planetToMoveTo!= null)
            return Command().createMoveCommand(robot.robotId, planetToMoveTo.planetId, player.playerId!!)
        else
            return null
    }



    private fun findOptimalRobotAction(robot: Robot, player: Player, currentGame: Game): Command?{
        val strategy = strategyService.getStrategyByGame(currentGame)
        if (robot.job == RobotJob.FIGHTER) {
            testRefreshEnergyStratgyFor(robot,player)?.let { return it }
            testAttackStrategyFor(robot,player)?.let { return it }
            testFullInventoryStrategyFor(robot,player)?.let { return it }
            testMoveStrategyFor(robot,player)?.let { return it }
        }
        else if (robot.job.isMiner()) {
            testRefreshEnergyStratgyFor(robot, player)?.let { return it }
            testFullInventoryStrategyFor(robot, player)?.let { return it }
            testAttackStrategyFor(robot, player)?.let { return it }
            testMoveStrategyFor(robot,player)?.let { return it }
        }
        return null
    }




}