package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent

class ErrorEvent: AbstractEvent() {

    override val isValid: Boolean
        get() =  true
}