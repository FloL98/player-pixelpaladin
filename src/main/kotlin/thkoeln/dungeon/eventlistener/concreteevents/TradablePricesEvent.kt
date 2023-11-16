package thkoeln.dungeon.eventlistener.concreteevents



import thkoeln.dungeon.domainprimitives.Moneten

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonProcessingException
import lombok.*
import thkoeln.dungeon.domainprimitives.TradableItem
import thkoeln.dungeon.domainprimitives.TradableType
import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.TradableItemDto
import java.util.ArrayList



class TradablePricesEvent(
    val tradableItemDtos: Array<TradableItemDto>
) : AbstractEvent() {

    override val isValid: Boolean
        get() = tradableItemDtos.size > 0

    /**
     * As the body consists of an array, we need special treatment here ...
     * @param jsonString
     */
    /*override fun fillWithPayload(jsonString: String) {
        try {
            val objectMapper = ObjectMapper().findAndRegisterModules()
            val tradableItemDtos = objectMapper.readValue(jsonString, Array<TradableItemDto>::class.java)
            for (tradableItemDto in tradableItemDtos) {
                val tradableItem = TradableItem(
                    tradableItemDto.name,
                    Moneten.fromInteger(tradableItemDto.price),
                    TradableType.valueOf(tradableItemDto.type?: "")
                )
                tradableItems.add(tradableItem)
            }
        } catch (conversionFailed: JsonProcessingException) {
            logger.error("Error converting payload for TradablePricesEvent with jsonString $jsonString")
        }
    }*/

    /*override fun toString(): String {
        var retVal = "TradablePricesEvent: $eventHeader"
        retVal += if (tradableItems.size == 0) {
            "\n\tNo tradablePriceDtos!"
        } else {
            """
	${tradableItems[0]} (plus ${tradableItems.size - 1} more)"""
        }
        return retVal
    }

    fun toStringDetailed(): String {
        var retVal = "TradablePricesEvent: $eventHeader"
        for (tradableItem in tradableItems) {
            retVal += """
                $tradableItem
                
                """.trimIndent()
        }
        return retVal
    }*/
}