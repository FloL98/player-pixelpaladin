package thkoeln.dungeon.player.application


import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.*
import thkoeln.dungeon.eventlistener.concreteevents.BankAccountTransactionBookedEvent
import thkoeln.dungeon.eventlistener.concreteevents.BankClearedEvent
import thkoeln.dungeon.eventlistener.concreteevents.BankInitializedEvent
import thkoeln.dungeon.game.application.GameApplicationService
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.player.domain.PlayerRepository
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.restadapter.PlayerRegistryDto
import thkoeln.dungeon.robot.application.RobotApplicationException
import thkoeln.dungeon.robot.application.RobotApplicationService
import thkoeln.dungeon.robot.application.RobotStrategyService
import thkoeln.dungeon.strategy.application.StrategyService
import java.util.*


/**
 * This class is for building connections to the rabbit queue and joining game
 * it handles incoming events related to balance, buying new robots and starts of the robot actions
 */
@Service
class PlayerApplicationService @Autowired constructor(
    private val playerRepository: PlayerRepository,
    private val gameApplicationService: GameApplicationService,
    private val gameServiceRESTAdapter: GameServiceRESTAdapter,
    private val robotApplicationService: RobotApplicationService,
    private val strategyService: StrategyService,
    private val robotStrategyService: RobotStrategyService,
) {
    private val logger = LoggerFactory.getLogger(PlayerApplicationService::class.java)
    private val modelMapper = ModelMapper()

    @Value("\${dungeon.playerName}")
    private val playerName: String? = null

    @Value("\${dungeon.playerEmail}")
    private val playerEmail: String? = null

    /**
     * Fetch the existing player. If there isn't one yet, it is created and stored to the database.
     * @return The current player.
     */
    fun queryAndIfNeededCreatePlayer(): Player {
        val player: Player
        val players = playerRepository.findAll()
        if (players.isNotEmpty()) {
            return players[0]
        } else {
            player = Player()
            player.assignNameAndEmail(playerName!!,playerEmail!!)
            player.resetToDefaultPlayerQueue()
            playerRepository.save(player)
            logger.info("Created new player (not yet registered): $player")
        }
        return player
    }

    /**
     * Register the current player (or do nothing, if it is already registered)
     */
    fun registerPlayer() {
        val player = queryAndIfNeededCreatePlayer()
        if (player.playerId != null) {
            logger.info("Player $player is already registered.")
            return
        }
        var playerRegistryDto = gameServiceRESTAdapter.sendGetRequestForPlayerId(player.name!!, player.email!!)

        if (playerRegistryDto == null) {
            playerRegistryDto = gameServiceRESTAdapter.sendPostRequestForPlayerId(player.name, player.email)
        }
        if (playerRegistryDto == null) {
            logger.error("Registration for player $player failed.")
            return
        }
        player.assignPlayerId(playerRegistryDto.playerId!!)
        player.assignPlayerQueue(playerRegistryDto.playerQueue!!)
        player.assignPlayerExchange((playerRegistryDto.playerExchange!!))
        playerRepository.save(player)
        logger.info("PlayerId sucessfully obtained for $player, is now registered.")
    }

    /**
     * Check if our player is not currently in a game, and if so, let him join the game -
     * if there is one, and it is open.
     */
    fun letPlayerJoinOpenGame() {
        logger.info("Trying to join game ...")
        val player = queryAndIfNeededCreatePlayer()
        val perhapsOpenGame = gameApplicationService.queryCreatedGame()
        if (!perhapsOpenGame.isPresent) {
            logger.info("No open game at the moment - cannot join a game.")
            return
        }
        val game = perhapsOpenGame.get()
        if (game.ourPlayerHasJoined == false) {
            gameServiceRESTAdapter.sendPutRequestToLetPlayerJoinGame(game.gameId!!, player.playerId!!)
            player.inGame = true
            playerRepository.save(player)
            game.ourPlayerHasJoined = true
            gameApplicationService.save(game)
            logger.info("Player successfully joined game $game , listening via player queue  ${player.playerQueue}")
        }
        else
            logger.info("Player already joined game $game !")
    }


    fun updatePlayerIngameStatus(status: Boolean){
        val player = queryAndIfNeededCreatePlayer()
        player.inGame = status
        playerRepository.save(player)
    }



    fun handleBankInitializedEvent(event: BankInitializedEvent){
        adjustBankAccount( event.balance!!)
    }

    fun handleBankClearedEvent(event: BankClearedEvent){
        adjustBankAccount(0)
    }

    fun handleBankAccountTransactionBookedEvent(event: BankAccountTransactionBookedEvent){
        addToBankAccount(event.transactionAmount!!)
    }

    private fun addToBankAccount(amount: Int){
        logger.info("Added to bank account $amount")
        val player = queryAndIfNeededCreatePlayer()
        val newMoney = Moneten.fromInteger((amount+ player.moneten.amount))
        player.moneten = newMoney
        playerRepository.save(player)
    }

    private fun adjustBankAccount(amount: Int) {
        logger.info("Adjust bank account to $amount")
        val newMoney = Moneten.fromInteger(amount)
        val player = queryAndIfNeededCreatePlayer()
        player.moneten = newMoney
        playerRepository.save(player)
    }

    /**
     * Buys new robots via REST command to Game service
     * @param numOfNewRobots
     */

    fun buyRobots() {
        val player = queryAndIfNeededCreatePlayer()

        val currentGame = gameApplicationService.queryRunningGame()
            .orElseThrow { PlayerApplicationException("Can't buy robots without running game!") }

        if (currentGame?.gameStatus?.isRunning == true) {
            val strategy = strategyService.getStrategyByGame(currentGame)
            val priceForRobot = Moneten.fromInteger(100)
            var numOfNewRobots: Int = strategy.budgetForRobots.canBuyThatManyFor(priceForRobot)
            if (numOfNewRobots < 0) throw PlayerApplicationException("numOfNewRobots < 0")
            else if((robotApplicationService.getTotalNumberOfRobots()+ numOfNewRobots) > strategy.maxNumberOfRobots)
                numOfNewRobots = strategy.maxNumberOfRobots - robotApplicationService.getTotalNumberOfRobots()

            if(numOfNewRobots > 0) {
                val command = Command().createRobotPurchaseCommand(player.playerId!!,numOfNewRobots)
                /*val commandObject = CommandObject(
                    null, null, null, "ROBOT", numOfNewRobots
                )
                val command = Command(player.playerId!!, CommandType.BUYING.stringValue)
                command.commandObject = commandObject*/
                gameServiceRESTAdapter.sendPostRequestForCommand(command)
                logger.info("$numOfNewRobots Robots were bought!")
            }
        }

    }


    fun letRobotsPlayRound(){
        val player = queryAndIfNeededCreatePlayer()
        val currentGame = gameApplicationService.queryRunningGame()
                .orElseThrow { RobotApplicationException("Robots cant take action without running game!") }
        robotStrategyService.commandRobotsToTakeAction(player,currentGame)
    }
}