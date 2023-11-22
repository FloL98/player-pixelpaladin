package thkoeln.dungeon.robot.domain



import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import kotlinx.coroutines.sync.Mutex
import java.util.UUID
import thkoeln.dungeon.domainprimitives.CompassDirection
import thkoeln.dungeon.domainprimitives.Inventory
import thkoeln.dungeon.domainprimitives.MineableResourceType
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.restadapter.RobotDto


@Entity
@Table(indexes = [Index(columnList = "robotId"),Index(columnList = "job")])
@JsonIgnoreProperties
class Robot(

) {

    @Id
    @JsonIgnore
    val id: UUID = UUID.randomUUID()

    @JsonAlias("id")
    var robotId: UUID = UUID.randomUUID()
    var player: UUID = UUID.randomUUID()


    @ManyToOne
    //@JsonIgnore
    var planet: Planet = Planet()

    var alive: Boolean = true
    var maxHealth: Int = 0
    var maxEnergy: Int = 0
    var energyRegen: Int = 0
    var attackDamage: Int = 0
    var miningSpeed: Int = 0
    var health: Int = 0
    var energy: Int = 0
    var healthLevel: Int = 0
    var damageLevel: Int = 0
    var miningSpeedLevel: Int = 0
    var miningLevel: Int = 0
    var energyLevel: Int = 0
    var energyRegenLevel: Int = 0
    @Embedded
    var inventory: Inventory = Inventory.emptyInventory()


    //todo wie stark ist ein Roboter und welchen Einfluss hat es auf den gameplan
    val combatPower: Int
        get() =  damageLevel +healthLevel

    //todo wie gut farmt ein Roboter und welchen Einfluss hat es auf den gameplan
    val farmPower: Int
        get() =  miningSpeedLevel +miningLevel

    //job dictates, which resource the robot is supposed to farm (NOT which he CAN farm)
    var job: RobotJob = RobotJob.COAL_WORKER

    //todo moveHistory in strategy mit einbinden
    @ElementCollection(fetch = FetchType.EAGER)
    var moveHistory: MutableList<UUID> = ArrayList()

    override fun toString(): String {
        return "{RobotId: $robotId , Health: $health, Energy: $energy"
    }

    fun isOnFarmablePlanet():Boolean{
        return (this.planet.mineableResource!=null && this.planet.mineableResource?.currentAmount!! > 0)
    }

    fun canFarmOnCurrentPlanet():Boolean{
        return (this.isOnFarmablePlanet() && this.planet.mineableResource?.resourceType?.neededMiningLevel() == this.miningLevel )
    }

    fun hasSuitableJobForResource(resourceType: MineableResourceType): Boolean{
        return (this.job.minesWhichType() == resourceType)
    }

    fun getNeighbourByDirection(direction: CompassDirection): Planet?{
        return when(direction){
            CompassDirection.NORTH -> this.planet.northNeighbour
            CompassDirection.EAST -> this.planet.eastNeighbour
            CompassDirection.SOUTH -> this.planet.southNeighbour
            CompassDirection.WEST -> this.planet.westNeighbour
        }
    }

    fun getDirectionByNeighbour(neighbour: Planet): CompassDirection?{
        return when(neighbour){
            this.planet.northNeighbour -> CompassDirection.NORTH
            this.planet.eastNeighbour -> CompassDirection.EAST
            this.planet.southNeighbour -> CompassDirection.SOUTH
            this.planet.westNeighbour -> CompassDirection.WEST
            else -> null
        }
    }

    fun getAllNeighbourPlanets(): ArrayList<Planet>{
        val neighbours: ArrayList<Planet> = ArrayList()
        for(direction in CompassDirection.entries){
            if(getNeighbourByDirection(direction)!= null)
                neighbours.add(getNeighbourByDirection(direction)!!)
        }
        return neighbours
    }


    companion object {
        fun fromDto(robotDto: RobotDto, planet:Planet): Robot {
            val robot = Robot()
            robot.player= robotDto.player
            robot.robotId = robotDto.robotId
            robot.planet = planet
            robot.alive = robotDto.alive
            robot.miningLevel = robotDto.miningLevel
            robot.damageLevel = robotDto.damageLevel
            robot.healthLevel = robotDto.healthLevel
            robot.miningSpeedLevel = robotDto.miningSpeedLevel
            robot.energyLevel = robotDto.energyLevel
            robot.energyRegenLevel = robotDto.energyRegenLevel
            robot.health = robotDto.health
            robot.attackDamage = robotDto.attackDamage
            robot.energy = robotDto.energy
            robot.energyRegen = robotDto.energyRegen
            robot.maxEnergy = robotDto.maxEnergy
            robot.maxHealth = robotDto.maxHealth
            robot.miningSpeed = robotDto.miningSpeed
            robot.inventory = robotDto.inventory
            return robot
        }
    }
}