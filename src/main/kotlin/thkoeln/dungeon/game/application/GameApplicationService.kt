package thkoeln.dungeon.game.application


import thkoeln.dungeon.game.domain.GameStatus
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.game.domain.GameException
import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.game.domain.GameRepository
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.TradableItem
import thkoeln.dungeon.eventlistener.concreteevents.TradablePricesEvent
import java.util.*


@Service
class GameApplicationService @Autowired constructor(
    private val gameRepository: GameRepository,
    private val gameServiceRESTAdapter: GameServiceRESTAdapter,
    private val environment: Environment
) {
    private val logger = LoggerFactory.getLogger(GameApplicationService::class.java)
    var modelMapper = ModelMapper()

    /**
     * Throw away all stored games, and fetch the currently active game (if any).
     */
    fun fetchRemoteGame() {
        gameRepository.deleteAll()
        var openGameDtos = gameServiceRESTAdapter.sendGetRequestForAllActiveGames()
        //erneute Abfrage, da manchmal bug, dass es in der queue ein created-event gibt, dann wird versucht das
        //Game zu fetchen, jedoch wird kein game gefunden und somit ist es nicht möglich dem spiel beizutreten
        if(openGameDtos.isEmpty())
            openGameDtos = gameServiceRESTAdapter.sendGetRequestForAllActiveGames()
        if (openGameDtos.isNotEmpty()) {
            val game = Game()
            modelMapper.map(openGameDtos[0], game)
            game.checkIfOurPlayerHasJoined(
                    openGameDtos[0]?.participatingPlayers, environment.getProperty("dungeon.playerName")
            )
            gameRepository.save(game)
            logger.info("Open game found: $game")
            if (openGameDtos.size > 1) logger.warn("More than one open game found!")
        }
    }


    /**
     * @return The currently available (created or started) open game
     */
    fun queryActiveGame(): Optional<Game> {
        val foundGames = gameRepository.findAllByGameStatusBetween(GameStatus.CREATED, GameStatus.STARTED)
        if (foundGames.size > 1) throw GameException("More than one available game!")
        return if (foundGames.size == 1) {
            Optional.of(foundGames[0])
        } else {
            Optional.empty()
        }
    }

    /**
     * @return The currently available (created) open game
     */
    fun queryCreatedGame(): Optional<Game> {
        val foundGames1 = gameRepository.findAll()
        var foundGames = foundGames1.filter { game -> game.gameStatus == GameStatus.CREATED }
        if (foundGames.size > 1) throw GameException("More than one created game!")
        return if (foundGames.size == 1) {
            Optional.of(foundGames[0])
        } else {
            Optional.empty()
        }
    }

    /**
     * @return The currently running (started) game
     */
    fun queryRunningGame(): Optional<Game> {
        val foundGames1 = gameRepository.findAll()
        var foundGames = foundGames1.filter { game -> game.gameStatus == GameStatus.STARTED }
        if (foundGames.size > 1) throw GameException("More than one running game!")
        return if (foundGames.size == 1) {

            Optional.of(foundGames[0])
        } else {
            Optional.empty()
        }
    }
    /**
     * We received notice (by event) that a certain game has started.
     * In that case, we simply assume that there is only ONE game currently running, and that it is THIS
     * game.
     */
    fun startGame(gameId: UUID?) {
        changeGameStatus(gameId, GameStatus.STARTED)
    }

    /**
     * We received notice (by event) that a certain game has finished.
     * @param gameId
     */
    fun finishGame(gameId: UUID?) {
        changeGameStatus(gameId, GameStatus.ENDED)
    }

    /**
     * We received notice (by event) that a certain game has finished.
     * @param gameId
     */
    private fun changeGameStatus(gameId: UUID?, gameStatus: GameStatus) {
        logger.info("Change status for game with gameId $gameId to $gameStatus")
        if (gameId == null) throw GameException("gameId == null")
        val perhapsGame = queryActiveGame()
        if (!perhapsGame.isPresent) {
            logger.error("No game with id $gameId found!")
            return
        }
        val game = perhapsGame.get()
        game.gameStatus = gameStatus
        gameRepository.save(game)
    }

    /**
     * updates shop of the current game
     */
    fun handleTradablePricesEvent(event: TradablePricesEvent){
        val game = queryRunningGame().orElseThrow{GameException("Couldnt update shop because no game currently active!")}
        if(game.shop.size == 0){ //Da sich der Shop eh nicht ändert, reicht es ihn einmal zu updaten aktuell
            for(itemDto in event.tradableItemDtos){
                game.shop.add(TradableItem.fromDto(itemDto))
            }
        }
        //game.shop = event.tradableItemDtos.toMutableList()
        gameRepository.save(game)
    }

    /**
     * updates the current round number
     */
    fun updateRoundCount(roundNumber: Int){
        val game = queryRunningGame().orElseThrow { GameException("Couldnt update roundRound because no game currently active") }
        game.currentRoundNumber = roundNumber
        gameRepository.save(game)
    }

    /**
     * @return if specific game is started
     */
    fun isGameStarted(gameId: UUID):Boolean{
        val games = gameRepository.findByGameId(gameId)
        return !(games.isEmpty() || games[0].gameStatus != GameStatus.STARTED)
    }

    /**
     * @return if specific game is created
     */
    fun isGameCreated(gameId: UUID):Boolean{
        val games = gameRepository.findByGameId(gameId)
        return !(games.isEmpty() || games[0].gameStatus != GameStatus.CREATED)
    }

    fun save(game: Game){
        gameRepository.save(game)
    }
}