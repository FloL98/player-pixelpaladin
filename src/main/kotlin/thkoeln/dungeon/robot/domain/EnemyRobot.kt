package thkoeln.dungeon.robot.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.RobotLevels
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.RevealedRobotDto
import thkoeln.dungeon.player.application.PlayerEventListener
import java.util.*

@Entity
@Table(indexes = [Index(columnList = "robotId")])
class EnemyRobot {
    @Id
    @JsonIgnore
    var id: UUID = UUID.randomUUID()
    var robotId: UUID =  UUID.randomUUID()
    var planetId: UUID =  UUID.randomUUID()
    var playerNotion: String = ""
    var health: Int = 0
    var energy: Int = 0
    var levels: RobotLevels = RobotLevels()
    @JsonIgnore
    var alive: Boolean = true


    fun createFromRevealedRobotDto(revealedRobotDto: RevealedRobotDto): EnemyRobot{
        val modelMapper = ModelMapper()
        modelMapper.configuration.setAmbiguityIgnored(true)
        val enemyRobot = EnemyRobot()
        modelMapper.map(revealedRobotDto,enemyRobot)
        return enemyRobot
    }

}