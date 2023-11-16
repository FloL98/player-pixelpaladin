package thkoeln.dungeon.eventlistener.concreteevents

import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.domainprimitives.MineableResource
import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class ResourceMinedEvent(
    @JsonProperty("planet")
    var planetId: UUID,
    var minedAmount: Int,
    var resource: MineableResource

): AbstractEvent(){

    override val isValid: Boolean
        get() = minedAmount >= 0 && resource.resourceType != null && resource.currentAmount != null && resource.maxAmount != null
}