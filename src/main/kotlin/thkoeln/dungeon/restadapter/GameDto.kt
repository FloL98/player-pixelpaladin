package thkoeln.dungeon.restadapter



import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID
import thkoeln.dungeon.game.domain.GameStatus


@JsonIgnoreProperties(ignoreUnknown = true)
class GameDto {
    var gameId: UUID? = null
    var gameStatus: GameStatus? = null
    var maxRounds: Int? = null
    var currentRoundNumber: Int? = null
    var roundLengthInMillis: Int? = null
    var participatingPlayers: Array<String> = arrayOf()
        private set

}