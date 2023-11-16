package thkoeln.dungeon.domainprimitives

import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.TradableItemDto
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.restadapter.RobotDto
import thkoeln.dungeon.robot.domain.Robot


/**
 * Domain Primitive to represent tradable items
 */
@Embeddable
class TradableItem(
    val name: String,
    @Embedded
    val price: Moneten,
    val type: TradableType
){

    //constructor()

    /*constructor(name: String?, price: Moneten?, type: TradableType?){
        this.name = name
        this.price = price
        this.type = type
    }*/

    companion object {
        fun fromDto(tradableItemDto: TradableItemDto): TradableItem {
            return TradableItem(tradableItemDto.name, Moneten.fromInteger(tradableItemDto.price),tradableItemDto.type)
        }
    }


}