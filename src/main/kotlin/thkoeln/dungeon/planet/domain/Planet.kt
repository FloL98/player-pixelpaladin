package thkoeln.dungeon.planet.domain



import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.lang.IllegalAccessException
import java.lang.reflect.InvocationTargetException
import java.lang.NoSuchMethodException
import kotlin.Throws
import org.apache.commons.text.WordUtils
import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.*
import thkoeln.dungeon.restadapter.PlanetShortDto
import java.lang.reflect.Method
import java.util.*
import kotlin.jvm.Transient


@Entity
@JsonIgnoreProperties
class Planet {
    @Id
    @JsonIgnore
    var id = UUID.randomUUID()
        private set

    // this is the EXTERNAL id that we receive from MapService. We could use this also as our own id, but then
    // we'll run into problems in case MapService messes up their ids. So, better we better keep these two apart.
    lateinit var planetId: UUID



    var isSpaceStation = false


    var visited = false
    fun hasBeenVisited(): Boolean {
        return visited
    }


    var name: String? = null

    // Flag needed for recursive output of all planets ... I know, this is not ideal, but couldn't yet
    // think of a better solution.

    var temporaryProcessingFlag: Boolean? = null

    @OneToOne(cascade = [CascadeType.MERGE])
    var northNeighbour: Planet? = null
        //protected set

    @OneToOne(cascade = [CascadeType.MERGE])
    var eastNeighbour: Planet? = null
        //protected set

    @OneToOne(cascade = [CascadeType.MERGE])
    var southNeighbour: Planet? = null
        //protected set

    @OneToOne(cascade = [CascadeType.MERGE])
    var westNeighbour: Planet? = null
        //protected set

    @Embedded
    @AttributeOverride(name="mineableResource", column=Column(nullable=true))
    var mineableResource: MineableResource? = null //MineableResource()


    /*der wert dieser Variablen sollte eigentlich schon in mineableResource gespeichert sein, hat aber
        Probleme beim mapping von json -> object gemacht
        muss noch gefixt werden*/
    @JsonProperty("resourceType")
    var _resourceType: MineableResourceType? = null

    @Embedded
    @JsonIgnore
    var movementDifficulty: MovementDifficulty = MovementDifficulty.fromInteger(1)


    var gameWorldId: UUID = UUID.randomUUID()

    @Transient
    private var logger = LoggerFactory.getLogger(Planet::class.java)

    constructor(planetId: UUID) {
        this.planetId = planetId
    }

    constructor()

    /**
     * Just for testing ...
     */
    constructor(name: String?) {
        this.name = name
        planetId = UUID.randomUUID()
    }

    /**
     * A neighbour relationship is always set on BOTH sides.
     * @param otherPlanet
     * @param direction
     */
    //nullable types ersetzen durch not noluuable
    fun defineNeighbour(otherPlanet: Planet?, direction: CompassDirection) {
        if (otherPlanet == null) throw PlanetException("Cannot establish neighbouring relationship with null planet!")
        try {
            val otherGetter = neighbouringGetter(direction.oppositeDirection)
            val setter = neighbouringSetter(direction)
            setter.invoke(this, otherPlanet)
            val remoteNeighbour = otherGetter.invoke(otherPlanet) as Planet?
            if (this != remoteNeighbour) {
                val otherSetter = neighbouringSetter(direction.oppositeDirection)
                otherSetter.invoke(otherPlanet, this)
            }
        } catch (e: IllegalAccessException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        } catch (e: InvocationTargetException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        } catch (e: NoSuchMethodException) {
            throw PlanetException("Something went wrong that should not have happened ..." + e.stackTrace)
        }
        closeNeighbouringCycleForAllDirectionsBut(direction)
    }

    fun closeNeighbouringCycleForAllDirectionsBut(notInThisDirection: CompassDirection) {
        for (compassDirection in CompassDirection.entries) {
            if (compassDirection == notInThisDirection) continue
            val neighbour = getNeighbour(compassDirection)
            if (neighbour != null) {
                for (ninetyDegrees in compassDirection.ninetyDegrees()) {
                    if (getNeighbour(ninetyDegrees) != null && neighbour.getNeighbour(ninetyDegrees) != null && getNeighbour(
                            ninetyDegrees
                        )!!.getNeighbour(compassDirection) == null
                    ) {
                        getNeighbour(ninetyDegrees)!!.defineNeighbour(
                            neighbour.getNeighbour(ninetyDegrees), compassDirection
                        )
                    }
                }
            }
        }
    }

    fun resetAllNeighbours() {
        northNeighbour = null
        westNeighbour = null
        eastNeighbour = null
        southNeighbour = null
    }

    fun getNeighbour1(compassDirection: CompassDirection): Planet?{
        return when(compassDirection){
            CompassDirection.NORTH-> this.northNeighbour
            CompassDirection.EAST-> this.eastNeighbour
            CompassDirection.SOUTH-> this.southNeighbour
            CompassDirection.WEST-> this.westNeighbour
        }
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
        val allNeighboursMap: MutableMap<CompassDirection, Planet> = HashMap()
        if (northNeighbour != null) allNeighboursMap[CompassDirection.NORTH] = northNeighbour!!
        if (westNeighbour != null) allNeighboursMap[CompassDirection.WEST] = westNeighbour!!
        if (eastNeighbour != null) allNeighboursMap[CompassDirection.EAST] = eastNeighbour!!
        if (southNeighbour != null) allNeighboursMap[CompassDirection.SOUTH] = southNeighbour!!
        return allNeighboursMap
    }

    /**
     * Add the neighbours to an existing 2d array of planets - grow the array if needed.
     * @param existingLocalIsland
     * @param localCoordinate - position where this planet is in the array
     * @return
     */
    fun constructLocalIsland(
        existingLocalIsland: TwoDimDynamicArray<Planet?>, localCoordinate: Coordinate?
    ): TwoDimDynamicArray<Planet?> {
        if (temporaryProcessingFlag == true) return existingLocalIsland
        temporaryProcessingFlag = true
        var localIsland = existingLocalIsland
        val allNeighbours = allNeighbours()
        for ((direction, neighbour) in allNeighbours) {
            if (neighbour.temporaryProcessingFlag == false) {
                val newCoordinate = localIsland.putAndEnhance(localCoordinate, direction, neighbour)
                localIsland = neighbour.constructLocalIsland(localIsland, newCoordinate)
            }
        }
        return localIsland
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Planet) return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    override fun toString(): String {
        return name ?: ("S: " + isSpaceStation + ", " + planetId)
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
    }
}