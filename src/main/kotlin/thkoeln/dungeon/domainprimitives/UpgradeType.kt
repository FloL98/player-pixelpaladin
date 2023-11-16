package thkoeln.dungeon.domainprimitives

import thkoeln.dungeon.robot.domain.RobotJob

/**
 * Domain Primitive to represent different upgrade types
 */
enum class UpgradeType(var stringValue: String) {
    STORAGE("STORAGE"),
    HEALTH("HEALTH"),
    DAMAGE("DAMAGE"),
    MINING_SPEED("MINING_SPEED"),
    MINING("MINING"),
    MAX_ENERGY("MAX_ENERGY"),
    ENERGY_REGEN("ENERGY_REGEN");


    fun isFarmingOriented(): Boolean{
        return (this != HEALTH && this != DAMAGE )
    }

    fun isFightingOriented(): Boolean{
        return (this == HEALTH || this == DAMAGE)
    }
}