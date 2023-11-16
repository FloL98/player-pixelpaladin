package thkoeln.dungeon.restadapter



import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.*
import thkoeln.dungeon.domainprimitives.*
import java.util.*

/**
 * Adapter for sending Game and Player life cycle calls to GameService
 */
@Component
class GameServiceRESTAdapter @Autowired constructor(private val restTemplate: RestTemplate) {
    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)

    @Value("\${dungeon.services.game}")
    private val gameServiceUrlString: String? = null
    private val objectMapper = ObjectMapper()



    // toDo hier tritt manchmal fehler auf, dass kein game gefunden wird, obwohl es eins gab
    /**
     * @return An array of all games (as DTOs) that are either CREATED or RUNNING
     */
    fun sendGetRequestForAllActiveGames(): Array<GameDto?> {
        val allGames: Array<GameDto>?
        val openGames: Array<GameDto?>
        val urlString = "$gameServiceUrlString/games"
        try {
            allGames = restTemplate.getForObject(urlString, Array<GameDto>::class.java)
            if (allGames.isNullOrEmpty()) {
                logger.warn("Received a null GameDto array from $urlString")
                return arrayOfNulls(0)
            }
            logger.info("Got " + allGames.size + " game(s) via REST ...")
            openGames = Arrays.stream(allGames).filter { gameDto: GameDto -> gameDto.gameStatus!!.isActive }.toArray{size -> arrayOfNulls<GameDto>(size)}
        } catch (e: RestClientException) {
            logger.error("Error when contacting " + urlString + ", message: " + e.message)
            throw RESTAdapterException(urlString, e)
        }
        return openGames
    }

    fun sendGetRequestForPlayerId(playerName: String, email: String): PlayerRegistryDto? {
        val urlString = "$gameServiceUrlString/players?name=$playerName&mail=$email"
        val returnedPlayerRegistryDto: PlayerRegistryDto? = try {
            restTemplate.execute(urlString, HttpMethod.GET, requestCallback(), playerRegistryResponseExtractor())
        } catch (e: RestClientResponseException) {
            return if (e.rawStatusCode == 404) {
                // actually, the proper answer would be an empty array, not 404.
                logger.info("No player exists for $playerName and $email")
                null
            } else {
                logger.error("Return code " + e.rawStatusCode + " for request " + urlString)
                throw RESTAdapterException(urlString, e)
            }
        } catch (e: RestClientException) {
            logger.error("Problem with the GET request '" + urlString + "', msg: " + e.message)
            throw RESTAdapterException(urlString, e)
        }
        logger.info("Player is already registered, with playerId: ${returnedPlayerRegistryDto?.playerId}")
        return returnedPlayerRegistryDto
    }

    fun sendPostRequestForPlayerId(playerName: String?, email: String?): PlayerRegistryDto? {
        logger.info("about to register player ...")
        val requestDto = PlayerRegistryDto()
        requestDto.name = playerName
        requestDto.email = email
        val urlString = "$gameServiceUrlString/players"
        var returnedPlayerRegistryDto: PlayerRegistryDto? = null
        try {
            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(requestDto)
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(json, headers)
            returnedPlayerRegistryDto = restTemplate.postForObject(urlString, request, PlayerRegistryDto::class.java)
        } catch (e: JsonProcessingException) {
            logger.error("Unexpected error converting requestDto to JSON: $requestDto")
            throw RESTAdapterException("Unexpected error converting requestDto to JSON: $requestDto")
        } catch (e: RestClientException) {
            logger.error("Problem with connection to server, cannot register player! Exception: " + e.message)
            throw RESTAdapterException(urlString, e)
        }
        logger.info("Registered player via REST, got playerId: ${returnedPlayerRegistryDto.playerId}")
        return returnedPlayerRegistryDto
    }

    fun sendPostRequestForCommand(command: Command): UUID {
        val urlString = "$gameServiceUrlString/commands"
        var commandAnswerDto: CommandAnswerDto? = null
        try {
            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(command)
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(json, headers)
            commandAnswerDto = restTemplate.postForObject(urlString, request, CommandAnswerDto::class)
        } catch (e: JsonProcessingException) {
            logger.error("Unexpected error converting command to JSON: $command")
            throw RESTAdapterException("Unexpected error converting requestDto to JSON: $command")
        } catch (e: RestClientException) {
            logger.error("Problem with connection to server! Exception: " + e.message)
            throw RESTAdapterException(urlString, e.message!!, HttpStatus.BAD_REQUEST)
        }
        val transactionId = commandAnswerDto?.transactionId
        return transactionId!!
    }

    /**
     * Register a specific player for a specific game via call to GameService endpoint.
     * Caveat: GameService returns somewhat weird error codes (non-standard).
     * @param gameId of the game
     * @param playerId of the player
     */
    fun sendPutRequestToLetPlayerJoinGame(gameId: UUID, playerId: UUID) { //:String {
        val urlString = "$gameServiceUrlString/games/$gameId/players/$playerId"
        logger.info("Try to sendPutRequestToLetPlayerJoinGame at: $urlString")
        return try {
            restTemplate.put(urlString,null)
        } catch (e: RestClientException) {
            logger.error("Exception encountered in sendPutRequestToLetPlayerJoinGame")
            if (e.message != null && e.message!!.contains("Player is already participating")) {
                logger.info("Player is already participating")
            }
            throw RESTAdapterException(urlString, e)
        }
    }



    private fun requestCallback(): RequestCallback {
        return RequestCallback { clientHttpRequest: ClientHttpRequest ->
            clientHttpRequest.headers.contentType = MediaType.APPLICATION_JSON
        }
    }

    private fun playerRegistryResponseExtractor(): ResponseExtractor<PlayerRegistryDto> {
        return ResponseExtractor { response: ClientHttpResponse ->
            objectMapper.readValue(
                response.body,
                PlayerRegistryDto::class.java
            )
        }
    }

    private fun playerJoinResponseExtractor(): ResponseExtractor<PlayerJoinDto> {
        return ResponseExtractor { response: ClientHttpResponse ->
            objectMapper.readValue(
                response.body,
                PlayerJoinDto::class.java
            )
        }
    }
}