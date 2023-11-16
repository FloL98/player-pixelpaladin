package thkoeln.dungeon.restadapter



import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID
import org.springframework.beans.factory.annotation.Value

@JsonIgnoreProperties(ignoreUnknown = true)
class PlayerRegistryDto {
    @Value("\${dungeon.playerName}")
    var name: String? = null

    @Value("\${dungeon.playerEmail}")
    var email: String? = null
    var playerId: UUID? = null
    var playerExchange: String? = null
    var playerQueue: String? = null

}