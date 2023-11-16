package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class BankAccountTransactionBookedEvent(
    var playerId: UUID,
    var transactionAmount: Int,
    var balance: Int
) : AbstractEvent() {

    override val isValid: Boolean
        get() = balance >= 0
}