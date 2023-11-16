package thkoeln.dungeon.eventlistener.concreteevents




import java.util.UUID
import thkoeln.dungeon.game.domain.GameStatus
import thkoeln.dungeon.eventlistener.AbstractEvent

data class GameStatusEvent(
    var gameId: UUID,
    var status: GameStatus
) : AbstractEvent() {

    override val isValid: Boolean
        get() =  true
}