package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.RevealedRobotDto


data class RobotsRevealedEvent(
    val robots: ArrayList<RevealedRobotDto> = ArrayList()
): AbstractEvent() {

    override val isValid: Boolean
        get() = true


}