package thkoeln.dungeon.robot.domain



import org.springframework.data.repository.CrudRepository
import java.util.*

interface RobotRepository : CrudRepository<Robot, UUID> {

    fun removeRobotByAlive(alive: Boolean)

    fun findByJob(robotJob: RobotJob):List<Robot>
}