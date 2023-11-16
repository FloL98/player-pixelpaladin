package thkoeln.dungeon.eventlistener.concreteevents



import java.util.UUID
import thkoeln.dungeon.eventlistener.AbstractEvent


data class BankInitializedEvent(
    var playerId: UUID,
    var balance: Int
) : AbstractEvent() {

    override val isValid: Boolean
        get() = balance >= 0
}