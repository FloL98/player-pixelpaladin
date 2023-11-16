package thkoeln.dungeon


import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import thkoeln.dungeon.player.application.PlayerApplicationService

@Component
class DungeonPlayerInitializer : InitializingBean {

    @Autowired
    private val playerApplicationService: PlayerApplicationService? = null

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        playerApplicationService?.registerPlayer()
    }
}