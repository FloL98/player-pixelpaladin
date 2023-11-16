package thkoeln.dungeon.strategy.domain

/**
 * should represent values, how much to spend for certain categories
 */
enum class Policy(var percentageMoneyForRobots: Int, var percentageMoneyForMiningUpgrades: Int, var percentageMoneyForFightingUpgrades: Int) {
    EARLY_POOR(100, 0,0),
    EARLY_RICH(90, 10, 0),
    MID_POOR(20, 70, 10),
    MID_RICH(10, 70, 20),
    LATE_POOR(10, 65, 25),
    LATE_RICH(10, 60, 30);


    companion object {
        fun fromInt(value: Int) : Policy {
            return if(value<0)
                entries.first { it.ordinal == 0 }
            else if(value > entries.size-1)
                entries.first { it.ordinal == entries.size-1 }
            else entries.first { it.ordinal == value }
        }
    }


}
