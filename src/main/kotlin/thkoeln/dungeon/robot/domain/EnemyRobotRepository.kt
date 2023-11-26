package thkoeln.dungeon.robot.domain

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface EnemyRobotRepository : CrudRepository<EnemyRobot, UUID> {

    fun removeRobotByRobotId(robotId: UUID)

    fun removeRobotByAlive(alive: Boolean)
    fun findAllByRobotIdIn(robotIdList: List<UUID> )

    fun findAllByPlanetId(planetId: UUID): List<EnemyRobot>

    @Transactional
    @Modifying
    @Query("Delete From EnemyRobot e Where e NOT In :elements")
    fun deleteAllNotInList(elements: Iterable<EnemyRobot>)


}