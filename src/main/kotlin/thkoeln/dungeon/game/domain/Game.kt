package thkoeln.dungeon.game.domain


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.TradableItem
import java.util.*
import kotlin.collections.ArrayList

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
class Game {
    @Id
    val id = UUID.randomUUID()

    // this is the EXTERNAL id that we receive from GameService. We could use this also as our own id, but then
    // we'll run into problems in case GameService messes up their ids (e.g. start the same game twice, etc.) So,
    // we better keep these two apart.
    var gameId: UUID? = null
    var gameStatus: GameStatus = GameStatus.CREATED
    var currentRoundNumber: Int = 1
    var maxRounds: Int = 0

    var ourPlayerHasJoined: Boolean = false

    @ElementCollection(fetch = FetchType.EAGER)
    var shop: MutableList<TradableItem> = ArrayList()

    @Transient
    val logger = LoggerFactory.getLogger(Game::class.java)
    fun resetToNewlyCreated() {
        gameStatus = GameStatus.CREATED
        currentRoundNumber = 0
        ourPlayerHasJoined = false
        logger.warn("Reset game $this to CREATED!")
    }

    /**
     * Can be called with the String[] of joined player names
     * @param namesOfJoinedPlayers
     */
    fun checkIfOurPlayerHasJoined(namesOfJoinedPlayers: Array<String>?, playerName: String?) {
        if (namesOfJoinedPlayers == null || playerName == null) throw GameException("namesOfJoinedPlayers == null || playerName == null")
        val found = Arrays.stream(namesOfJoinedPlayers).anyMatch { s: String -> s == playerName }
        ourPlayerHasJoined = found
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val game = o as Game
        return id == game.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    override fun toString(): String {
        return "Game (" + gameStatus + ", " + gameId + ")"
    }


    companion object {
        fun newlyCreatedGame(gameId: UUID?): Game {
            val game = Game()
            game.gameId = gameId
            game.resetToNewlyCreated()
            return game
        }
    }
}