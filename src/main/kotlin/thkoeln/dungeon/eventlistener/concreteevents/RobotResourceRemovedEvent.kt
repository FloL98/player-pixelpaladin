package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.domainprimitives.InventoryResource
import thkoeln.dungeon.domainprimitives.MineableResourceType
import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class RobotResourceRemovedEvent(
    var robotId: UUID,
    var removedAmount: Int,
    var removedResource: MineableResourceType,
    var resourceInventory: InventoryResource,
): AbstractEvent() {


    override val isValid: Boolean
        get() = removedAmount >= 0
}