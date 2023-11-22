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
import thkoeln.dungeon.game.application.GameApplicationService
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.planet.domain.PlanetDomainService
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.strategy.application.StrategyService
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.robot.domain.RobotJob
import thkoeln.dungeon.robot.domain.robotactionstrategies.FighterActionStrategy
import thkoeln.dungeon.robot.domain.robotactionstrategies.MinerActionStrategy
import java.util.ArrayList


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
    private val gameApplicationService: GameApplicationService,
) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)

    private val gameWorld = GameWorld(strategyService,robotApplicationService,planetDomainService,robotEventHandleService)


    //Feedback:
    //strategy-pattern
    //ki-strategien
    //"gameworld"- als ganze strukturen (modelle) übergeben, die alle Infos enthalten, um passende strategy zu bestimmen
    //jede aktion/strategy mit zahl 1-10 bewerten


    fun commandRobotsToTakeAction(player: Player?, currentGame: Game){
        val robots: List<Robot> = robotApplicationService.getAllRobots()
        logger.info("${robots.size} robots currently exists!")
        for(robot in robots){
            val command = findOptimalRobotAction(robot, player!!, currentGame)
            if(command != null) {
                //logger.info("robotcommand : ${command.commandType}")
                gameServiceRESTAdapter.sendPostRequestForCommand(command)
            }
        }
    }

    private fun findOptimalRobotAction(robot: Robot, player: Player, currentGame: Game): Command?{
        val strategy = strategyService.getStrategyByGame(currentGame)
        return if (robot.job == RobotJob.FIGHTER) {
            FighterActionStrategy(gameWorld, currentGame, robot, player, strategy).getCommand()
        } else if (robot.job.isMiner())
            MinerActionStrategy(gameWorld, currentGame,robot, player, strategy).getCommand()
        else
            null
    }


    private val commandList = ArrayList<Command>()

    fun fillCommandList(player: Player?, currentGame: Game){
        val robots: List<Robot> = robotApplicationService.getAllRobots()
        logger.info("${robots.size} robots currently exists!")
        for(robot in robots){
            val command = findOptimalRobotAction(robot, player!!, currentGame)
            if(command != null) {
                //logger.info("robotcommand : ${command.commandType}")
                commandList.add(command)
            }
        }

        logger.info("Done with filling command list!")
    }

    fun executeCommandList(){
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
            val maxThreads = 3 + commandList.size / 75
            val robotCommandDispatcher = Dispatchers.IO.limitedParallelism(maxThreads)
            commandList.forEach { command ->
                launch(robotCommandDispatcher) {
                    gameServiceRESTAdapter.sendPostRequestForCommand(command)
                    //logger.info("http command ${command.commandType} successful")
                }
            }
        }
    }

    fun clearCommandList(){
        while(commandList.isNotEmpty())
            commandList.removeFirst()
    }


}