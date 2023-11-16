package thkoeln.dungeon.domainprimitives


import com.fasterxml.jackson.annotation.JsonProperty
import java.util.ArrayList

/**
 * Domain Primitive to represent a compass direction
 */
enum class CompassDirection {
    @JsonProperty("NORTH")
    NORTH,
    @JsonProperty("EAST")
    EAST,
    @JsonProperty("SOUTH")
    SOUTH,
    @JsonProperty("WEST")
    WEST;

    val oppositeDirection: CompassDirection
        get() {
            return when (this) {
                NORTH -> SOUTH
                EAST -> WEST
                SOUTH -> NORTH
                WEST -> EAST
            }
        }

    fun xOffset(): Int {
        return when (this) {
            NORTH -> 0
            EAST -> 1
            SOUTH -> 0
            WEST -> -1
        }
    }

    fun yOffset(): Int {
        return when (this) {
            NORTH -> 1
            EAST -> 0
            SOUTH -> -1
            WEST -> 0
        }
    }

    fun ninetyDegrees(): List<CompassDirection> {
        val retVals: MutableList<CompassDirection> = ArrayList()
        when (this) {
            NORTH -> {
                retVals.add(WEST)
                retVals.add(EAST)
            }
            EAST -> {
                retVals.add(NORTH)
                retVals.add(SOUTH)
            }
            SOUTH -> {
                retVals.add(WEST)
                retVals.add(EAST)
            }
            WEST -> {
                retVals.add(NORTH)
                retVals.add(SOUTH)
            }
        }
        return retVals
    }
}