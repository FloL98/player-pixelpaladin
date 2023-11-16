package thkoeln.dungeon.restadapter



import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID


@JsonIgnoreProperties(ignoreUnknown = true)
class CommandAnswerDto {
    var transactionId: UUID? = null
}