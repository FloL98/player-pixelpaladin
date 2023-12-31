package thkoeln.dungeon.planet.domain



import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.apache.commons.text.WordUtils
import thkoeln.dungeon.domainprimitives.*
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.PlanetShortDto
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

@Entity
@Table(indexes = [Index(columnList = "planetId")])
@JsonIgnoreProperties
class Planet {

    @Id
    var planetId: UUID = UUID.randomUUID()

    var visited: Boolean = false

    @OneToOne
    var northNeighbour: Planet? = null

    @OneToOne
    var eastNeighbour: Planet? = null

    @OneToOne
    var southNeighbour: Planet? = null

    @OneToOne
    var westNeighbour: Planet? = null

    @Embedded
    var mineableResource: MineableResource? = null


    /*der wert dieser Variablen sollte eigentlich schon in mineableResource gespeichert sein, hat aber
        Probleme beim mapping von json -> object gemacht
        muss noch gefixt werden*/
    @JsonProperty("resourceType")
    var _resourceType: MineableResourceType? = null

    @Embedded
    @JsonIgnore
    var movementDifficulty: MovementDifficulty = MovementDifficulty.fromInteger(1)

    var gameWorldId: UUID = UUID.randomUUID()

    constructor(planetId: UUID) {
        this.planetId = planetId
    }

    constructor()


    fun resetAllNeighbours() {
        northNeighbour = null
        westNeighbour = null
        eastNeighbour = null
        southNeighbour = null
    }


    fun getNeighbour(compassDirection: CompassDirection?): Planet? {
        return try {
            val getter = neighbouringGetter(compassDirection)
            getter.invoke(this) as Planet?
        } catch (e: IllegalAccessException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        } catch (e: InvocationTargetException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        } catch (e: NoSuchMethodException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        }
    }

    @Throws(NoSuchMethodException::class)
    fun neighbouringGetter(direction: CompassDirection?): Method {
        val name = "get" + WordUtils.capitalize(WordUtils.swapCase(direction.toString())) + "Neighbour"
        return this.javaClass.getDeclaredMethod(name)
    }

    @Throws(NoSuchMethodException::class)
    fun neighbouringSetter(direction: CompassDirection?): Method {
        val name = "set" + WordUtils.capitalize(WordUtils.swapCase(direction.toString())) + "Neighbour"
        return this.javaClass.getDeclaredMethod(name, *arrayOf<Class<*>>(this.javaClass))
    }

    fun allNeighbours(): Map<CompassDirection, Planet> {
        val allNeighboursMap: MutableMap<CompassDirection, Planet> = EnumMap(CompassDirection::class.java)
        if (northNeighbour != null) allNeighboursMap[CompassDirection.NORTH] = northNeighbour!!
        if (westNeighbour != null) allNeighboursMap[CompassDirection.WEST] = westNeighbour!!
        if (eastNeighbour != null) allNeighboursMap[CompassDirection.EAST] = eastNeighbour!!
        if (southNeighbour != null) allNeighboursMap[CompassDirection.SOUTH] = southNeighbour!!
        return allNeighboursMap
    }

    fun getAllNeighborsAsList(): List<Planet>{
        val neighbors = ArrayList<Planet>()
        for(direction in CompassDirection.entries) {
            if(this.getNeighbour(direction) != null)
                neighbors.add(this.getNeighbour(direction)!!)
        }
        return neighbors
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Planet) return false
        return planetId == o.planetId
    }

    override fun hashCode(): Int {
        return Objects.hash(planetId)
    }



    companion object {
        fun fromDto(planetShortDto: PlanetShortDto): Planet {
            val planet = Planet()
            planet.planetId = planetShortDto.planetId
            planet._resourceType = planetShortDto.resourceType
            planet.movementDifficulty = MovementDifficulty.fromInteger(planetShortDto.movementDifficulty)
            planet.gameWorldId = planetShortDto.gameWorldId
            return planet
        }

        fun fromPlanetIdAndMovementDifficultyAndResource(planetId: UUID, movementDifficulty: Int, resource: MineableResource?): Planet{
            val planet = Planet()
            planet.planetId = planetId
            planet.movementDifficulty = MovementDifficulty.fromInteger(movementDifficulty)
            planet.mineableResource = resource
            planet._resourceType = resource?.resourceType
            return planet
        }
    }
}