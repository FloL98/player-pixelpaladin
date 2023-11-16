package thkoeln.dungeon.robot.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.modelmapper.ModelMapper
import thkoeln.dungeon.domainprimitives.RobotLevels
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.RevealedRobotDto
import java.util.*

@Entity
class EnemyRobot {
    @Id
    var id: UUID = UUID.randomUUID()
    var robotId: UUID? = null
    var planetId: UUID? = null
    var playerNotion: String = ""
    var health: Int = 0
    var energy: Int = 0
    var levels: RobotLevels = RobotLevels()


    fun createFromRevealedRobotDto(revealedRobotDto: RevealedRobotDto): EnemyRobot{
        val modelMapper = ModelMapper()
        val enemyRobot = EnemyRobot()
        modelMapper.map(revealedRobotDto,enemyRobot)
        return enemyRobot
    }

}