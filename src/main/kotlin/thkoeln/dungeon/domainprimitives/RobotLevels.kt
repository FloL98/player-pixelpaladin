package thkoeln.dungeon.domainprimitives

import jakarta.persistence.Embeddable


@Embeddable
class RobotLevels {
    var healthLevel: Int = 0
    var damageLevel: Int = 0
    var miningSpeedLevel: Int = 0
    var miningLevel: Int = 0
    var energyLevel: Int = 0
    var energyRegenLevel: Int = 0
    var storageLevel: Int = 0

    fun getSumOfAllLevels(): Int = healthLevel+damageLevel+miningLevel+miningSpeedLevel+energyLevel+energyRegenLevel+storageLevel


    fun getSumOfFightingLevels(): Int = healthLevel+damageLevel

    fun getSumOfFarmingLevels(): Int = miningLevel+miningSpeedLevel+energyLevel+energyRegenLevel+storageLevel

}