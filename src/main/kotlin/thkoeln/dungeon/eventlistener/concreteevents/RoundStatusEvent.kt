package thkoeln.dungeon.eventlistener.concreteevents




import java.util.UUID
import thkoeln.dungeon.domainprimitives.RoundStatusType
import thkoeln.dungeon.eventlistener.AbstractEvent

data class RoundStatusEvent(
    val gameId: UUID,
    val roundId: UUID,
    val roundNumber: Int,
    val roundStatus: RoundStatusType
) : AbstractEvent() {
    override val isValid: Boolean
        get() = roundNumber >= 0

    override fun toString(): String {
        return "gameId: $gameId , roundId: $roundId, roundNumber: $roundNumber, roundStatus:$roundStatus"
    }



}