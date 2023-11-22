package thkoeln.dungeon.robot.domain



import org.springframework.data.repository.CrudRepository
import java.util.*

interface RobotRepository : CrudRepository<Robot, UUID> {

    fun findByRobotId(robotId: UUID): Optional<Robot>

    fun removeRobotByRobotId(robotId: UUID)

    fun removeRobotByAlive(alive: Boolean)

    fun findByJob(robotJob: RobotJob):List<Robot>
}