package thkoeln.dungeon



import org.springframework.beans.factory.annotation.Autowired
import thkoeln.dungeon.player.application.PlayerApplicationService
import org.springframework.http.HttpStatus
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import java.util.stream.Collectors
import java.io.IOException
import org.springframework.web.server.ResponseStatusException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

/**
 * (c) Tobi, https://github.com/The-Microservice-Dungeon/gamelog/blob/main/src/main/java/com/github/tmd/gamelog/core/LogsEndpoint.java
 */
@Component
@Endpoint(id = "logs")
@Slf4j
class DungeonPlayerLogEndpoint @Autowired constructor(@param:Value("\${logging.file.path}") private val logFilePath: String) {
    protected var logger = LoggerFactory.getLogger(PlayerApplicationService::class.java)

    /**
     * GET /actuator/logs
     */
    @ReadOperation(produces = [MediaType.TEXT_PLAIN_VALUE])
    fun logs(): String {
        val path = Paths.get("$logFilePath/spring.log")
        try {
            val lines = Files.lines(path)
            val data = lines.collect(Collectors.joining("\n"))
            lines.close()
            return data
        } catch (e: IOException) {
            logger.error("Could not read log file", e)
        }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}