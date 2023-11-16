package thkoeln.dungeon


import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.game.application.GameApplicationService
import thkoeln.dungeon.player.application.PlayerApplicationService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Service

@Service
class DungeonPlayerStartupService @Autowired constructor(
    private val playerApplicationService: PlayerApplicationService,
    private val gameApplicationService: GameApplicationService
) : ApplicationListener<ApplicationReadyEvent> {
    private val logger = LoggerFactory.getLogger(DungeonPlayerStartupService::class.java)

    /**
     * In this method, the player participation is prepared. If there are problems (connection
     * problems, no running game, etc.) the player waits 10s and tries again.
     * @param event
     */
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val player = playerApplicationService.queryAndIfNeededCreatePlayer()
        if (!player.hasJoinedGame()) {
            try {
                gameApplicationService.fetchRemoteGame()
                playerApplicationService.letPlayerJoinOpenGame()
                //playerApplicationService.searchForOpenGameAndJoin()
            } catch (exc: DungeonPlayerRuntimeException) {
                logger.error("Error when initializing player: " + exc.message)
            }
        }
    }
}