package thkoeln.dungeon.eventlistener.concreteevents.eventdtos

import thkoeln.dungeon.domainprimitives.TradableType

data class TradableItemDto(
    val name: String,
    val price: Int,
    val type: TradableType,
) {
}