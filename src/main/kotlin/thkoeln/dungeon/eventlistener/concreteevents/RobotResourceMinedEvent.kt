package thkoeln.dungeon.eventlistener.concreteevents

import thkoeln.dungeon.domainprimitives.InventoryResource
import thkoeln.dungeon.domainprimitives.MineableResourceType
import thkoeln.dungeon.eventlistener.AbstractEvent
import java.util.*

data class RobotResourceMinedEvent(
    var robotId: UUID,
    var minedAmount: Int,
    var minedResource: MineableResourceType,
    var resourceInventory: InventoryResource
): AbstractEvent() {

    override val isValid: Boolean
        get() = minedAmount >= 0
}