package thkoeln.dungeon.game.domain



import java.util.UUID
import org.springframework.data.repository.CrudRepository

interface GameRepository : CrudRepository<Game, UUID> {
    fun findByGameId(gameId: UUID): List<Game>
    fun existsByGameId(gameId: UUID): Boolean
    fun findAllByGameStatusEquals(gameStatus: GameStatus): List<Game>
    fun findAllByGameStatusBetween(gameStatus1: GameStatus, gameStatus2: GameStatus): List<Game>
    override fun findAll(): List<Game>
}