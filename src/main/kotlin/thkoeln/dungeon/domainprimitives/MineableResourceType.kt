package thkoeln.dungeon.domainprimitives


/**
 * Domain Primitive to represent the type of resource
 */
enum class MineableResourceType {
    COAL, IRON, GEM, GOLD, PLATIN;

    fun neededMiningLevel():Int{
        return this.ordinal
    }
}