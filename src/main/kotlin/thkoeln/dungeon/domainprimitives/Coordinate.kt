package thkoeln.dungeon.domainprimitives


import jakarta.persistence.Embeddable



/*
 * For convenience (compatibility with our reading directions), Coordinates seen as growing from top to bottom
 * and from left to right. I.e. it looks like this:
 *
 *        x---->
 *         0  1  2  3  4
 *   y   0
 *   |   1
 *   |   2
 *   V   3
 */
@Embeddable
class Coordinate {
    var x: Int = 0
    var y: Int = 0

    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    constructor()

    fun isSmallerEqualsThan(anotherCoordinate: Coordinate): Boolean {
        return  x <= anotherCoordinate.x && y <= anotherCoordinate.y
    }

    fun isLargerThan(anotherCoordinate: Coordinate): Boolean {
        return  x > anotherCoordinate.x || y > anotherCoordinate.y
    }

    fun neighbourCoordinate(compassDirection: CompassDirection): Coordinate {
        if (compassDirection == null) throw DomainPrimitiveException("compassDirection must not be null.")
        var newX = x
        var newY = y
        if (compassDirection == CompassDirection.NORTH) newY = Math.max(newY!! - 1, 0)
        if (compassDirection == CompassDirection.EAST) newX++
        if (compassDirection == CompassDirection.SOUTH) newY++
        if (compassDirection == CompassDirection.WEST) newX = Math.max(newX!! - 1, 0)
        return fromInteger(newX, newY)
    }

    override fun toString(): String {
        return "($x,$y)"
    }

    override fun equals(other: Any?): Boolean { //TODO: Wird noch gefixt
        if (this === other) return true
        if (other !is Coordinate) return false
        return this.x == other.x && this.y == other.y
    }

    override fun hashCode(): Int { //TODO: Wird noch gefixt
        var result = x
        result = 31 * result + y
        return result
    }

    companion object {
        @JvmStatic
        fun fromInteger(x: Int?, y: Int?): Coordinate {
            if (x == null) throw DomainPrimitiveException("x must not be null!")
            if (y == null) throw DomainPrimitiveException("y must not be null!")
            if (x < 0) throw DomainPrimitiveException("x must be >= 0: $x")
            if (y < 0) throw DomainPrimitiveException("y must be >= 0: $y")
            return Coordinate(x, y)
        }

        /**
         * @param coordinateString the coordinate in form of a string e.g. (1,2)
         */
        fun fromString(coordinateString: String): Coordinate {
            val coords =
                coordinateString.replace("\\(".toRegex(), "").replace("\\)".toRegex(), "").split(",").toTypedArray()
            if (coords.size != 2) throw DomainPrimitiveException("Not a valid string")
            val x = Integer.valueOf(coords[0])
            val y = Integer.valueOf(coords[1])
            return Coordinate(x, y)
        }

        /**
         * The first coordinate ever assigned is (0,0) - the map is built starting from here. This
         * allows negative coordinates as well.
         */
        @JvmStatic
        fun initialCoordinate(): Coordinate {
            return Coordinate(0, 0)
        }
    }
}