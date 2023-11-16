package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent


class UnknownEvent : AbstractEvent() {

    override val isValid: Boolean
        get() = true
}