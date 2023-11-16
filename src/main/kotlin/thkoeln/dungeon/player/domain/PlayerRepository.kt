package thkoeln.dungeon.player.domain



import java.util.UUID
import org.springframework.data.repository.CrudRepository



interface PlayerRepository : CrudRepository<Player, UUID> {
    override fun findAll(): List<Player>
    fun findByPlayerId(playerId: UUID): List<Player>
}