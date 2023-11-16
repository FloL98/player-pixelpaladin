package thkoeln.dungeon.robot.domain

import thkoeln.dungeon.domainprimitives.MineableResourceType

enum class RobotJob() {
    COAL_WORKER,
    IRON_WORKER,
    GEM_WORKER,
    GOLD_WORKER,
    PLATIN_WORKER,
    FIGHTER,
    EXPLORER;


    fun minesWhichType(): MineableResourceType?{
        when(this){
            COAL_WORKER -> return MineableResourceType.COAL
            IRON_WORKER -> return MineableResourceType.IRON
            GEM_WORKER -> return MineableResourceType.GEM
            GOLD_WORKER -> return MineableResourceType.GOLD
            PLATIN_WORKER -> return MineableResourceType.PLATIN
            else -> return null
        }
    }

    fun isMiner(): Boolean{
        return (this != FIGHTER && this != EXPLORER)
    }

    fun isFighter(): Boolean{
        return this == FIGHTER
    }
    fun isExplorer(): Boolean{
        return this == EXPLORER
    }

}