package thkoeln.dungeon.player.application


import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.RoundStatusType
import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.eventlistener.EventFactory
import thkoeln.dungeon.eventlistener.EventHeader
import thkoeln.dungeon.eventlistener.EventType
import thkoeln.dungeon.eventlistener.concreteevents.*
import thkoeln.dungeon.game.application.GameApplicationService
import thkoeln.dungeon.game.domain.GameStatus
import thkoeln.dungeon.planet.application.PlanetApplicationService
import thkoeln.dungeon.robot.application.RobotApplicationService
import thkoeln.dungeon.robot.application.RobotEventHandleService
import thkoeln.dungeon.robot.application.RobotStrategyService
import thkoeln.dungeon.strategy.application.StrategyService
import java.util.concurrent.CountDownLatch

/**
 * This service class listen to the messages queue and redirect incoming events
 */
@Service
class PlayerEventListener @Autowired constructor(
    private val environment: Environment,
    private var eventFactory: EventFactory,
    private val gameApplicationService: GameApplicationService,
    private val playerApplicationService: PlayerApplicationService,
    private val robotApplicationService: RobotApplicationService,
    private val planetApplicationService: PlanetApplicationService,
    private val strategyService: StrategyService,
    private val robotEventHandleService: RobotEventHandleService,
    private val robotStrategyService: RobotStrategyService,
) {
    private val logger = LoggerFactory.getLogger(PlayerEventListener::class.java)

    private var applicationEventPublisher: ApplicationEventPublisher? = null
    @Autowired
    fun PlayerEventListener(
        eventFactory: EventFactory?,
        applicationEventPublisher: ApplicationEventPublisher?
    ) {
        this.eventFactory = eventFactory!!
        this.applicationEventPublisher = applicationEventPublisher
    }

    //this attribute is used to synchronize started-event and robotsRevealedEvent to send commands after these two happended
    private var commandLatch = CountDownLatch(2)

    val coroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * Listener to all events that the core services send to the player
     * @param eventIdStr
     * @param transactionIdStr
     * @param playerIdStr
     * @param type
     * @param version
     * @param timestampStr
     * @param payload
     */

    @RabbitListener(queues = ["player-\${dungeon.playerName}"])
    fun receiveEvent(
        @Header(defaultValue = "", value = EventHeader.EVENT_ID_KEY) eventIdStr: String?,
        @Header(defaultValue = "", value = EventHeader.TRANSACTION_ID_KEY) transactionIdStr: String?,
        @Header(defaultValue = "", value = EventHeader.PLAYER_ID_KEY) playerIdStr: String?,
        @Header(EventHeader.TYPE_KEY) type: String,
        @Header(defaultValue = "", value = EventHeader.VERSION_KEY) version: String,
        @Header(EventHeader.TIMESTAMP_KEY) timestampStr: String,
        payload: String,
        message: Message
    ) {

            //if(type == "RoundStatus" ||type =="RobotsRevealed") {
            logger.info(
                """${environment.getProperty("ANSI_BLUE")}====> received event ... 
	 {type=$type, eventId=$eventIdStr, transactionId=$transactionIdStr, playerId=$playerIdStr, version=$version, timestamp=${timestampStr}
	$payload${environment.getProperty("ANSI_RESET")}"""
            )
            //}


            val eventHeader = EventHeader(type, eventIdStr, playerIdStr, transactionIdStr, timestampStr, version)
            val newEvent = eventFactory.fromHeaderAndPayload(eventHeader, payload)
            if (!newEvent.isValid) {
                logger.error("Event invalid: $newEvent")
                return
            }



            if (playerIdStr == "public" || playerIdStr == playerApplicationService.queryAndIfNeededCreatePlayer().playerId.toString()) {
                if (eventHeader.eventType.isRobotRelated) {
                    coroutineScope.launch {
                        //logger.info("coroutine start")
                        handleRobotRelatedEvent(newEvent)
                        //logger.info("coroutine end")
                    }
                }
                else if(eventHeader.eventType.isPlanetRelated){
                    coroutineScope.launch {
                        logger.info("coroutine start")
                        handlePlanetRelatedEvent(newEvent)
                        logger.info("coroutine end")
                    }
                }
                else if(eventHeader.eventType.isPlayerRelated){
                    coroutineScope.launch {
                        //logger.info("coroutine start")
                        handleOtherEventParallel(newEvent)
                        //logger.info("coroutine end")
                    }
                }
                //events werden nicht behandelt
                else if(eventHeader.eventType.isNotImportant)
                else {

                    runBlocking {
                        coroutineScope.coroutineContext.job.children.forEach { it.join() }
                    }
                    //logger.info("start")
                    handleOtherEvent(newEvent)
                    //logger.info("end")
                }
            }

    }

    private suspend fun handleRobotRelatedEvent(event: AbstractEvent){
        if(gameApplicationService.queryRunningGame().isPresent) {
            when (event.eventHeader?.eventType) {
                EventType.ROBOT_SPAWNED -> handleRobotSpawnedIntegrationEvent(event as RobotSpawnedEvent)
                EventType.ROBOT_HEALTH_UPDATED -> handleRobotHealthUpdatedEvent(event as RobotHealthUpdatedEvent)
                EventType.ROBOT_MOVED -> handleRobotMovedIntegrationEvent(event as RobotMovedEvent)
                EventType.ROBOT_UPGRADED -> handleRobotUpgradedIntegrationEvent(event as RobotUpgradedEvent)
                EventType.ROBOT_REGENERATED -> handleRobotRegeneratedIntegrationEvent(event as RobotRegeneratedEvent)
                EventType.ROBOT_RESOURCE_MINED -> handleRobotResourceMinedIntegrationEvent(event as RobotResourceMinedEvent)
                EventType.ROBOT_RESOURCE_REMOVED -> handleRobotResourceRemovedIntegrationEvent(event as RobotResourceRemovedEvent)
                EventType.ROBOT_ATTACKED -> handleRobotAttackedIntegrationEvent(event as RobotAttackedEvent)
                EventType.ROBOT_RESTORED_ATTRIBUTES -> handleRobotRestoredAttributesIntegrationEvent(event as RobotRestoredAttributesEvent)
                else -> {}
            }
        }
    }

    private suspend fun handlePlanetRelatedEvent(event: AbstractEvent){
        if(gameApplicationService.queryRunningGame().isPresent) {
            when (event.eventHeader?.eventType) {
                EventType.RESOURCE_MINED -> handleResourceMinedEvent(event as ResourceMinedEvent)
                EventType.PLANET_DISCOVERED -> handlePlanetDiscoveredEvent(event as PlanetDiscoveredEvent)
                else -> {}
            }
        }
    }

    private suspend fun handleOtherEventParallel(event: AbstractEvent){
        if(gameApplicationService.queryRunningGame().isPresent) {
            when (event.eventHeader?.eventType) {
                EventType.BANK_ACCOUNT_TRANSACTION_BOOKED -> handleBankAccountTransactionBookedEvent(event as BankAccountTransactionBookedEvent)
                else -> {}
            }
        }
    }


    private fun handleOtherEvent(event: AbstractEvent?) {

        if(gameApplicationService.queryRunningGame().isPresent) {
            when (event?.eventHeader?.eventType) {
                EventType.BANK_INITIALIZED -> handleBankInitializedEvent(event as BankInitializedEvent)
                EventType.ROUND_STATUS -> handleRoundStatusEvent(event as RoundStatusEvent)
                EventType.TRADABLE_PRICES -> handleTradablePricesEvent(event as TradablePricesEvent)
                EventType.ROBOTS_REVEALED -> handleRobotsRevealedEvent(event as RobotsRevealedEvent)
                else -> {}
            }
        }
        when (event?.eventHeader?.eventType) {
            EventType.GAME_STATUS -> handleGameStatusEvent(event as GameStatusEvent)
            EventType.BANK_CLEARED -> handleBankClearedEvent(event as BankClearedEvent)
            else ->{}
        }
    }

    private suspend fun handleRobotMovedIntegrationEvent(event: RobotMovedEvent){
        robotEventHandleService.handleRobotMovedEvent(event)
    }
    private suspend fun handleRobotAttackedIntegrationEvent(event: RobotAttackedEvent){
        robotEventHandleService.handleRobotAttackedEvent(event)
    }
    private suspend fun handleRobotUpgradedIntegrationEvent(event: RobotUpgradedEvent){
        robotEventHandleService.handleRobotUpgradedEvent(event)
    }
    private suspend fun handleRobotRegeneratedIntegrationEvent(event: RobotRegeneratedEvent){
        robotEventHandleService.handleRobotRegeneratedEvent(event)
    }
    private suspend fun handleRobotResourceMinedIntegrationEvent(event: RobotResourceMinedEvent){
        robotEventHandleService.handleRobotResourceMinedIntegrationEvent(event)
    }
    private suspend fun handleRobotResourceRemovedIntegrationEvent(event: RobotResourceRemovedEvent){
        robotEventHandleService.handleRobotResourceRemovedIntegrationEvent(event)
    }
    private suspend fun handleRobotRestoredAttributesIntegrationEvent(event: RobotRestoredAttributesEvent){
        robotEventHandleService.handleRobotRestoredAttributesIntegrationEvent(event)
    }
    private suspend fun handleRobotHealthUpdatedEvent(robotHealthUpdatedEvent: RobotHealthUpdatedEvent){
        robotEventHandleService.handleRobotHealthUpdatedEvent(robotHealthUpdatedEvent)
    }

    private suspend fun handleResourceMinedEvent(resourceMinedEvent: ResourceMinedEvent){
        planetApplicationService.handleResourceMinedEvent(resourceMinedEvent)
    }


    private suspend fun handleRobotSpawnedIntegrationEvent(robotSpawnedEvent: RobotSpawnedEvent){
        robotEventHandleService.handleRobotSpawnedIntegrationEvent(robotSpawnedEvent)
    }

    private suspend fun handleBankAccountTransactionBookedEvent(bankAccountTransactionBookedEvent: BankAccountTransactionBookedEvent){
        playerApplicationService.handleBankAccountTransactionBookedEvent(bankAccountTransactionBookedEvent)
    }

    private suspend fun handlePlanetDiscoveredEvent(planetDiscoveredEvent: PlanetDiscoveredEvent){
        planetApplicationService.handlePlanetDiscoveredEventWithoutMerge(planetDiscoveredEvent)

    }

    private fun handlePlanetDiscoveredEventThreadSafe(planetDiscoveredEvent: PlanetDiscoveredEvent){
        planetApplicationService.handlePlanetDiscoveredEventThreadSafe(planetDiscoveredEvent,coroutineScope)
    }

    private fun handleRobotsRevealedEvent(robotsRevealedEvent: RobotsRevealedEvent){
        robotEventHandleService.handleRobotRevealedEvent(robotsRevealedEvent)
        commandLatch.countDown()
        handleRobotsRevealedAndRoundStartedBothReady()
    }

    private fun handleGameStatusEvent(gameStatusEvent: GameStatusEvent) {
        if (GameStatus.CREATED == gameStatusEvent.status) {
            gameApplicationService.fetchRemoteGame()
            if(gameApplicationService.isGameCreated(gameStatusEvent.gameId)) {
                playerApplicationService.letPlayerJoinOpenGame()
            }
        } else if (GameStatus.STARTED == gameStatusEvent.status && gameApplicationService.isGameCreated(gameStatusEvent.gameId)) {
            strategyService.createStrategyForGameIfNotExists(gameStatusEvent.gameId)
            gameApplicationService.startGame(gameStatusEvent.gameId)
        } else if (GameStatus.ENDED == gameStatusEvent.status && gameApplicationService.isGameStarted(gameStatusEvent.gameId)) {
            gameApplicationService.finishGame(gameStatusEvent.gameId)
            robotApplicationService.deleteAllRobots()
            planetApplicationService.deleteAll()
            strategyService.deleteAllStrategies()
            robotApplicationService.resetEnemyRepository()
            playerApplicationService.updatePlayerIngameStatus(false)
            commandLatch = CountDownLatch(2)
            //playerApplicationService.searchForOpenGameAndJoin()
        }
    }


    private fun handleBankInitializedEvent(bankInitializedEvent: BankInitializedEvent) {
        playerApplicationService.handleBankInitializedEvent(bankInitializedEvent)
    }

    private fun handleBankClearedEvent(bankClearedEvent: BankClearedEvent){
        playerApplicationService.handleBankClearedEvent(bankClearedEvent)
    }

    private fun handleRoundStatusEvent(event: RoundStatusEvent) {
        val player = playerApplicationService.queryAndIfNeededCreatePlayer()
        val game = gameApplicationService.queryActiveGame().get()
        if(event.roundNumber==2){ //zur Strategyinitialisierung am Anfang, eventuell Verändern
            strategyService.updateStrategy(game, player.moneten, robotApplicationService.getTotalNumberOfRobots())
        }

        if(event.roundStatus == RoundStatusType.STARTED) {
            logger.info("----------------------------------------------\n----------------------------------------------\nROUND ${event.roundNumber} started!")
            logger.info("Balance: ${player.moneten.amount}")
            robotApplicationService.removeAllDeadRobots()
            gameApplicationService.updateRoundCount(event.roundNumber)
            strategyService.updateStrategy(game, player.moneten, robotApplicationService.getTotalNumberOfRobots())
            robotApplicationService.upgradeRobotsJobs(game) //nimmt viel zeit ein
            playerApplicationService.buyRobots()


            commandLatch.countDown()
            handleRobotsRevealedAndRoundStartedBothReady()

        }
        else if(event.roundStatus == RoundStatusType.COMMAND_INPUT_ENDED){
            commandLatch = CountDownLatch(2)
            logger.info("Command-input ended")

        }
        else  if(event.roundStatus == RoundStatusType.ENDED){
            logger.info("Round ended")
            //robotEventHandleService.resetCurrentEnemyList()
            robotStrategyService.clearCommandList()


            //robotStrategyService.fillCommandList(player,game)

        }

    }

    private fun handleTradablePricesEvent(event: TradablePricesEvent) {
        gameApplicationService.handleTradablePricesEvent(event)
    }

    /**
     * This method executes commands after robotsrevealedEvent and Round-started both happended each round
     */
    private fun handleRobotsRevealedAndRoundStartedBothReady(){
        if(commandLatch.count == 0L){
            val player = playerApplicationService.queryAndIfNeededCreatePlayer()
            val game = gameApplicationService.queryActiveGame().get()
            //robotStrategyService.fillCommandList(player,game)
            robotStrategyService.fillCommandList(player,game)
            robotStrategyService.executeCommandListParallel()
        }
    }
}