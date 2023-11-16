package thkoeln.dungeon.restadapter



import thkoeln.dungeon.DungeonPlayerRuntimeException
import org.springframework.http.HttpStatus
import java.lang.RuntimeException



/**
 * The connection to GameService could not be established (network failure or GameService down). The player
 * business logic needs to deal with this and try again later.
 */
class RESTAdapterException : DungeonPlayerRuntimeException {
    var endPoint: String? = null
        private set
    var returnValue: HttpStatus? = null
        private set

    constructor(message: String?) : super(message) {}
    constructor(endPoint: String, message: String, returnValue: HttpStatus?) : super(
        """Error in communication with GameService calling endpoint '$endPoint'. Message:
	$message
	Return value: ${returnValue ?: "unknown"}"""
    ) {
        this.endPoint = endPoint
        this.returnValue = returnValue
    }

    constructor(endPoint: String, originalException: RuntimeException) : super(
        """Error in communication with GameService calling endpoint '$endPoint'. Message:
	${originalException.message}"""
    ) {
    }
}