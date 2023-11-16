package thkoeln.dungeon.eventlistener


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID



import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonProcessingException
import jakarta.persistence.Embedded
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.slf4j.LoggerFactory

@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class AbstractEvent {
    @Id
    var localId = UUID.randomUUID()

    @Transient
    var logger = LoggerFactory.getLogger(AbstractEvent::class.java)

    @Embedded
    var eventHeader: EventHeader? = null



    var messageBodyAsJson: String? = null

    var processed = java.lang.Boolean.FALSE
    fun hasBeenProcessed(): Boolean {
        return processed
    }

    /**
     * @return true if the event was complete and consistent (enough) in order to be processed, false otherwise.
     * This is for the implementing concrete subclass to decide.
     */
    abstract val isValid: Boolean
    open fun fillWithPayload(jsonString: String) {
        try {
            val objectMapper = ObjectMapper().findAndRegisterModules()
            objectMapper.readerForUpdating(this).readValue<Any>(jsonString)
        } catch (conversionFailed: JsonProcessingException) {
            logger.error("Error converting payload for event with jsonString $jsonString")
        }
    }


}