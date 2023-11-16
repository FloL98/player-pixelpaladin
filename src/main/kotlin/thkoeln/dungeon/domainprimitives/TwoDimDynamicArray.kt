package thkoeln.dungeon.domainprimitives


import java.util.ArrayList

/**
 * 2dim Array als a list of rows of equal length, able to grow dynamically.
 * To my great surprise I haven't found a class "off the shelf" that can do this ... probably I didn't look
 * well enough, but so be it - here is a (unit tested) own implementation.
 *
 * For convenience (compatibility with our reading directions), the array is seen as growing from top to bottom
 * and from left to right. I.e. it looks like this:
 *
 * x---->
 * 0  1  2  3  4
 * y   0
 * |   1
 * |   2
 * V   3
 *
 * (c) Stefan Bente 2022
 * @param <T>
</T> */
class TwoDimDynamicArray<T>(maxX: Int, maxY: Int) {
    private val array = ArrayList<ArrayList<T?>>()

    constructor(max: Coordinate) : this(max.x!! + 1, max.y!! + 1) {}
    constructor(value: T) : this(1, 1) {
        put(Coordinate.Companion.initialCoordinate(), value)
    }

    fun sizeX(): Int {
        return array[0].size
    }

    fun sizeY(): Int {
        return array.size
    }

    val maxCoordinate: Coordinate
        get() = Coordinate.Companion.fromInteger(sizeX() - 1, sizeY() - 1)

    operator fun get(coordinate: Coordinate?): T? {
        if (coordinate == null) throw DomainPrimitiveException("coordinate must not be null")
        if (coordinate.isLargerThan(maxCoordinate)) throw DomainPrimitiveException("coordinate out of bounds: $coordinate")
        return array[coordinate.y][coordinate.x]
    }

    fun put(coordinate: Coordinate?, value: T) {
        if (coordinate == null) throw DomainPrimitiveException("coordinate must not be null")
        if (coordinate.isLargerThan(maxCoordinate)) throw DomainPrimitiveException("coordinate out of bounds: $coordinate")
        array[coordinate.y][coordinate.x] = value
    }

    /**
     * Put a value into the array no/we/so/ea of the given coordinate. Enhance the array if needed.
     * @param coordinate
     * @param compassDirection
     * @param value
     * @return the Coordinate of the (potentially enlarged) array, where the value is now located
     */
    fun putAndEnhance(coordinate: Coordinate?, compassDirection: CompassDirection?, value: T): Coordinate? {
        if (coordinate == null) throw DomainPrimitiveException("coordinate must not be null")
        if (coordinate.isLargerThan(maxCoordinate)) throw DomainPrimitiveException("coordinate out of bounds: $coordinate")
        if (compassDirection == null) throw DomainPrimitiveException("compassDirection must not be null")
        val whereToInsert = enhanceIfNeededAt(coordinate, compassDirection)
        put(whereToInsert, value)
        return whereToInsert
    }

    /**
     * Enhance the array at a given position if this is needed, in the compass direction specified
     * @param coordinate
     * @param compassDirection
     * @return
     */
    fun enhanceIfNeededAt(coordinate: Coordinate?, compassDirection: CompassDirection?): Coordinate? {
        if (coordinate == null) throw DomainPrimitiveException("coordinate must not be null")
        if (coordinate.isLargerThan(maxCoordinate)) throw DomainPrimitiveException("coordinate out of bounds: $coordinate")
        if (compassDirection == null) throw DomainPrimitiveException("compassDirection must not be null")
        val neighbourCoordinate = coordinate.neighbourCoordinate(compassDirection)
        if (neighbourCoordinate!!.isLargerThan(maxCoordinate) ||
            coordinate.y == 0 && compassDirection == CompassDirection.NORTH ||
            coordinate.x == 0 && compassDirection == CompassDirection.WEST
        ) {
            when (compassDirection) {
                CompassDirection.NORTH -> addRowAt(0)
                CompassDirection.EAST -> addColumnAt(coordinate.x + 1)
                CompassDirection.SOUTH -> addRowAt(coordinate.y + 1)
                CompassDirection.WEST -> addColumnAt(coordinate.x)
            }
        }
        return neighbourCoordinate
    }

    fun addRowAt(y: Int) {
        if (y < 0) throw DomainPrimitiveException("can't add row at index < 0: $y")
        if (y > sizeY()) throw DomainPrimitiveException("can't add row at index > sizeY: $y")
        if (y < sizeY()) array.add(y, createNullRow(sizeX())) else array.add(createNullRow(sizeX()))
    }

    fun addColumnAt(x: Int) {
        if (x < 0) throw DomainPrimitiveException("can't add column at index < 0: $x")
        if (x > sizeX()) throw DomainPrimitiveException("can't add column at index > sizeX: $x")
        for (y in 0 until sizeY()) {
            if (x < sizeX()) array[y].add(x, null) else array[y].add(null)
        }
    }

    override fun toString(): String {
        var retVal = ""
        for (y in 0 until sizeY()) {
            for (x in 0 until sizeX()) {
                retVal += get(Coordinate.Companion.fromInteger(x, y)).toString()
                retVal += "  ||  "
            }
            retVal += "\n"
        }
        return retVal
    }

    private fun createNullRow(sizeY: Int): ArrayList<T?> {
        val yArray = ArrayList<T?>()
        for (y in 0 until sizeY) {
            yArray.add(null)
        }
        return yArray
    }

    init {
        if (maxX <= 0 || maxY <= 0) throw DomainPrimitiveException("maxX / maxY must be > 0: $maxX, $maxY")
        for (y in 0 until maxY) {
            array.add(createNullRow(maxX))
        }
    }
}