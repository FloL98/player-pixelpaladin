package thkoeln.dungeon.restadapter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.ToString
import org.springframework.beans.factory.annotation.Value
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class GameRegistryDto {

    var maxRounds: Int? = 200
    var maxPlayers: Int? = 100
    var gameId: UUID? = null

}