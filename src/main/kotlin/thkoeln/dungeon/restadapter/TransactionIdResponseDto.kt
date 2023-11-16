package thkoeln.dungeon.restadapter



import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

/**
 * DTO for the response type that GameService sends back as an answer to all kinds of
 * commands: Just with a transactionId in it.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class TransactionIdResponseDto {
    var transactionId: UUID? = null
    val isValid: Boolean
        get() = transactionId != null
}