package thkoeln.dungeon.strategy.domain

import org.springframework.data.repository.CrudRepository
import thkoeln.dungeon.game.domain.Game
import java.util.*

interface StrategyRepository : CrudRepository<Strategy, UUID> {
    fun findByGame(game: Game?): Optional<Strategy>
}