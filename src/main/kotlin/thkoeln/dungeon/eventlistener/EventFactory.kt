package thkoeln.dungeon.eventlistener


import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import thkoeln.dungeon.eventlistener.concreteevents.*
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.TradableItemDto


@Service
class EventFactory {
    private val logger = LoggerFactory.getLogger(EventFactory::class.java)
    val objectMapper = ObjectMapper().findAndRegisterModules()
    fun fromHeaderAndPayload(eventHeader: EventHeader?, payload: String?): AbstractEvent {
        if (eventHeader == null || payload == null) throw DungeonEventException("eventHeader == null || payload == null")
        val newEvent: AbstractEvent = when (eventHeader.eventType) {
            EventType.GAME_STATUS -> objectMapper.readValue(payload, GameStatusEvent::class.java)
            EventType.BANK_INITIALIZED -> objectMapper.readValue(payload, BankInitializedEvent::class.java)//BankInitializedEvent()
            EventType.ROUND_STATUS -> objectMapper.readValue(payload, RoundStatusEvent::class.java)// RoundStatusEvent()
            EventType.TRADABLE_PRICES -> {
                val tradables = objectMapper.readValue(payload, Array<TradableItemDto>::class.java)
                TradablePricesEvent(tradables)
            }//TradablePricesEvent()
            EventType.BANK_CLEARED -> objectMapper.readValue(payload, BankClearedEvent::class.java)//BankClearedEvent()
            EventType.ROBOT_SPAWNED -> {//RobotSpawnedIntegrationEvent()
                objectMapper.readValue(payload, RobotSpawnedEvent::class.java)
            }
            EventType.ROBOTS_REVEALED -> objectMapper.readValue(payload, RobotsRevealedEvent::class.java)//RobotsRevealedIntegrationEvent()
            EventType.PLANET_DISCOVERED -> objectMapper.readValue(payload, PlanetDiscoveredEvent::class.java)//PlanetDiscoveredEvent()
            EventType.BANK_ACCOUNT_TRANSACTION_BOOKED -> objectMapper.readValue(payload, BankAccountTransactionBookedEvent::class.java)//BankAccountTransactionBookedEvent()
            EventType.RESOURCE_MINED -> objectMapper.readValue(payload, ResourceMinedEvent::class.java)//ResourceMinedEvent()
            EventType.ROBOT_HEALTH_UPDATED -> objectMapper.readValue(payload, RobotHealthUpdatedEvent::class.java)//RobotHealthUpdatedEvent()
            EventType.ROBOT_ATTACKED -> objectMapper.readValue(payload, RobotAttackedEvent::class.java)//RobotAttackedIntegrationEvent()
            EventType.ROBOT_MOVED -> objectMapper.readValue(payload, RobotMovedEvent::class.java)//RobotMovedIntegrationEvent()
            EventType.ROBOT_UPGRADED -> objectMapper.readValue(payload, RobotUpgradedEvent::class.java)//RobotUpgradedIntegrationEvent()
            EventType.ROBOT_REGENERATED -> objectMapper.readValue(payload, RobotRegeneratedEvent::class.java)//RobotRegeneratedIntegrationEvent()
            EventType.ROBOT_RESOURCE_MINED -> objectMapper.readValue(payload, RobotResourceMinedEvent::class.java)//RobotResourceMinedIntegrationEvent()
            EventType.ROBOT_RESOURCE_REMOVED -> objectMapper.readValue(payload, RobotResourceRemovedEvent::class.java)//RobotResourceRemovedIntegrationEvent()
            EventType.ROBOT_RESTORED_ATTRIBUTES -> objectMapper.readValue(payload, RobotRestoredAttributesEvent::class.java)//RobotRestoredAttributesIntegrationEvent()
            EventType.ERROR -> ErrorEvent()//objectMapper.readValue(payload, ErrorEvent::class.java) -> hat kein payload?
            else -> UnknownEvent()
        }
        newEvent.eventHeader = eventHeader
        //newEvent.fillWithPayload(payload)
        return newEvent
    }
}