package thkoeln.dungeon.eventlistener



import jakarta.persistence.Embeddable
import java.util.UUID
import java.lang.IllegalArgumentException
import org.slf4j.LoggerFactory


@Embeddable
class EventHeader(
    type: String, eventIdStr: String?, playerIdStr: String?, transactionIdStr: String?,
    timestampStr: String?, version: String?
) {
    constructor() : this("", null,null,null,null,null

    )


    @Transient
    private var logger = LoggerFactory.getLogger(EventHeader::class.java)

    var eventId: UUID? = null
        protected set
    var transactionId: UUID? = null
        protected set
    var playerId: UUID? = null
        protected set
    var broadcast = false
        protected set
    var eventType = EventType.UNKNOWN
    var eventTypeString: String? = null
        protected set
    var version: String? = null
        protected set
    var timestampString: String? = null
        protected set
    override fun toString(): String {
        return """Header:, {eventType=$eventType, eventTypeString=$eventTypeString
	, transactionId=$transactionId, eventId=$eventId, playerId=$playerId, isBroadcast=${broadcast}, eventType=$eventType, eventTypeString=$eventTypeString, version='$version', timestampString='$timestampString'}"""
    }

    companion object {
        const val EVENT_ID_KEY = "eventId"
        const val TRANSACTION_ID_KEY = "transactionId"
        const val PLAYER_ID_KEY = "playerId"

        // Player can be the string "public" instead of a real player ID; this means that the event goes to
        // all players (a broadcast), instead of just to one specific player.
        const val BROADCAST_EVENT_KEY = "public"
        const val TYPE_KEY = "type"
        const val VERSION_KEY = "version"
        const val TIMESTAMP_KEY = "timestamp"
    }

    init {
        try {
            if (eventIdStr != null && eventIdStr != "") eventId = UUID.fromString(eventIdStr)
            //scheinbar wird bei "RobotRevealedIntegrationEvent für die transactionId "null" (als String) übergeben
            //und muss deshalb darauf überprüft werden, da sonst ein Fehler auftritt bei "UUID.fromString(transactionIdStr)"
            if (transactionIdStr != null && transactionIdStr != "" && transactionIdStr != "null")
                transactionId = UUID.fromString(transactionIdStr)
            if (BROADCAST_EVENT_KEY == playerIdStr) {
                broadcast = true
            } else {
                broadcast = false
                if (playerIdStr != null && playerIdStr != "") playerId = UUID.fromString(playerIdStr)
            }
        } catch (e: IllegalArgumentException) {
            logger.error(
                "Unexpected error at converting UUIDs in event header: " +
                        eventIdStr + ", " + transactionIdStr + ", $playerIdStr"
            )
        }
        timestampString = timestampStr
        this.version = version
        eventType = EventType.findByStringValue(type)
        eventTypeString = type
    }
}