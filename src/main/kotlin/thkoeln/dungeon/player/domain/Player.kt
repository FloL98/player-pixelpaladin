package thkoeln.dungeon.player.domain


import jakarta.persistence.*
import thkoeln.dungeon.player.domain.NameGenerator.generateName
import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.Moneten
import java.util.*


@Entity
class Player {

    @Id
    var id = UUID.randomUUID()
        private set

    var name: String? = null

    var email: String? = null

    var playerId: UUID? = null

    var playerQueue: String? = null

    var playerExchange: String? = null

    var inGame: Boolean = false

    @Embedded
    var moneten = Moneten.fromInteger(0)

    /**
     * Choose a preconfiguredname and email for the player
     */
    fun assignPreconfiguredNameAndEmail(){
        name = "PixelPaladin"
        email = "flolema98@gmail.com"
    }

    fun assignNameAndEmail(name: String, email:String){
        this.name = name
        this.email = email
    }

    fun assignPlayerId(playerId: UUID) {
        this.playerId = playerId
    }

    fun assignPlayerQueue(playerQueue: String){
        this.playerQueue = playerQueue
    }

    fun assignPlayerExchange(playerExchange: String){
        this.playerExchange = playerExchange
    }



    fun resetToDefaultPlayerQueue() {
        if (this.name == null) throw PlayerException("name == null, so cant assign queue")
        this.playerQueue = "player-${this.name}"
    }

    val isRegistered: Boolean
        get() = playerId != null

    fun hasJoinedGame(): Boolean {
        return inGame
    }



    override fun toString(): String {
        return "Player '" + name + "' (email: " + email + ", playerId: " + playerId + ")"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Player) return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}