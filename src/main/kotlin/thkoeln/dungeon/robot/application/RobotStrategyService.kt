package thkoeln.dungeon.robot.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import thkoeln.dungeon.strategy.domain.GameWorld
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.planet.domain.PlanetDomainService
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.strategy.application.StrategyService
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.robot.domain.robotactionstrategies.*
import java.util.*


/**
 * Nutzt Strategy, Actionstrategien, Roboterattribute und Gameattribute, um die nächste Aktion der Roboter zu bestimmen
 */
@Service
class RobotStrategyService@Autowired constructor(
    private val robotApplicationService: RobotApplicationService,
    private val gameServiceRESTAdapter: GameServiceRESTAdapter,
    private val robotEventHandleService: RobotEventHandleService,
    private val strategyService: StrategyService,
    private val planetDomainService: PlanetDomainService,
) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)

    private val gameWorld = GameWorld(strategyService,robotApplicationService,planetDomainService,robotEventHandleService)

    private var commandList: MutableList<Command> = ArrayList<Command>()

    /*fun fillCommandList(player: Player?, currentGame: Game){
        val robots: List<Robot> = robotApplicationService.getAllRobots()
        logger.info("${robots.size} robots currently exists!")
        for(robot in robots){
            val command = findOptimalRobotAction(robot, player!!, currentGame)
            //val command = findOptimalRobotActionExperimental(robot, player!!, currentGame)
            if(command != null) {
                commandList.add(command)
            }
        }

        logger.info("Done with filling command list!")
    }*/

    fun fillCommandList(player: Player?, currentGame: Game){
        val robots: List<Robot> = robotApplicationService.getAllRobots()
        logger.info("${robots.size} robots currently exists!")
        val commandListNew = robots.mapNotNull { findOptimalRobotActionExperimental(it, player!!, currentGame) }.toMutableList()
        this.commandList = commandListNew
    }

    fun executeCommandListNotParallel(){
        var counter = 0
        for(command in commandList) {
            gameServiceRESTAdapter.sendPostRequestForCommand(command)
            logger.info("http command $counter successful")
            counter++
            if(counter == commandList.size){
                logger.info("Reached last element of http command list!!")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun executeCommandListParallel() {
        logger.info("http command start")
        CoroutineScope(Dispatchers.IO).launch {
            val maxThreads = 3 + commandList.size / 10 //75
            val robotCommandDispatcher = Dispatchers.IO.limitedParallelism(maxThreads)
            commandList.forEach { command ->
                launch(robotCommandDispatcher) {
                    gameServiceRESTAdapter.sendPostRequestForCommand(command)
                }
            }
        }
    }

    fun clearCommandList(){
        while(commandList.isNotEmpty())
            commandList.removeFirst()
    }


    private fun findOptimalRobotActionExperimental(robot: Robot, player: Player, currentGame: Game): Command?{
        val strategy = strategyService.getStrategyByGame(currentGame)

        val minerStrategies: List<RobotActionStrategy> = listOf(
            FullInventoryStrategy(currentGame, robot, player, strategy),
            RefreshEnergyStrategy(currentGame, robot, player, strategy),
            MinerAttackStrategy(this.gameWorld, currentGame, robot, player, strategy),
            MinerUpgradeStrategy(this.gameWorld, currentGame, robot, player, strategy),
            FarmResourceStrategy(currentGame, robot, player, strategy),
            MoveStrategy(this.gameWorld, currentGame, robot, player, strategy)
        )
        val fighterStrategies: List<RobotActionStrategy> = listOf(
            FullInventoryStrategy(currentGame, robot, player, strategy),
            RefreshEnergyStrategy(currentGame, robot, player, strategy),
            AttackEnemyStrategy(this.gameWorld, currentGame, robot, player, strategy),
            FighterUpgradeStrategy(this.gameWorld, currentGame, robot, player, strategy),
            MoveStrategy(this.gameWorld, currentGame, robot, player, strategy)
        )

        if(robot.job.isMiner()) {
            for (actionStrategy in minerStrategies) {
                val command = actionStrategy.getCommand()
                if (command != null) {
                    return command
                }
            }
        }
        else if(robot.job.isFighter()) {
            for (actionStrategy in fighterStrategies) {
                val command = actionStrategy.getCommand()
                if (command != null) {
                    return command
                }
            }
        }
        return null
    }


}