package thkoeln.dungeon.eventlistener.concreteevents


import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*


data class BankClearedEvent(
    var playerId: UUID,
    var balance: Int

) : AbstractEvent() {
    override val isValid: Boolean
        get() = balance == 0
}