package thkoeln.dungeon.eventlistener.concreteevents


import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.restadapter.RobotDto




data class RobotSpawnedEvent(
    @JsonProperty("robot")
    val robotDto: RobotDto
):  AbstractEvent() {


    override val isValid: Boolean
        get() = true





}