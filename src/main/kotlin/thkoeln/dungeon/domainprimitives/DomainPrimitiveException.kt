package thkoeln.dungeon.domainprimitives


import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import thkoeln.dungeon.DungeonPlayerRuntimeException

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Invalid domain primitive")
class DomainPrimitiveException(message: String?) : DungeonPlayerRuntimeException(message)