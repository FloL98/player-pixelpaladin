package thkoeln.dungeon.eventlistener




import java.util.*

enum class EventType(val stringValue: String) {
    GAME_STATUS("GameStatus"),
    BANK_INITIALIZED("BankAccountInitialized"),
    BANK_CLEARED("BankAccountCleared"),
    ROUND_STATUS("RoundStatus"),
    TRADABLE_PRICES("TradablePrices"),
    //ROBOT_SPAWNED("RobotSpawned"),
    PLANET_DISCOVERED("PlanetDiscovered"),
    ROBOT_SPAWNED("RobotSpawned"),
    ROBOTS_REVEALED("RobotsRevealed"),
    BANK_ACCOUNT_TRANSACTION_BOOKED("BankAccountTransactionBooked"),
    //ROBOT_ENERGY_UPDATED("RobotEnergyUpdated"),
    //ROBOT_MOVED("RobotMoved"),
    RESOURCE_MINED("ResourceMined"),
    //ROBOT_INVENTORY_UPDATED("RobotInventoryUpdated"),
    //ROBOT_UPGRADED("RobotUpgraded"),
    ROBOT_HEALTH_UPDATED("RobotHealthUpdated"),
    ROBOT_MOVED("RobotMoved"),
    ROBOT_ATTACKED("RobotAttacked"),
    ROBOT_REGENERATED("RobotRegenerated"),
    ROBOT_RESOURCE_MINED("RobotResourceMined"),
    ROBOT_RESOURCE_REMOVED("RobotResourceRemoved"),
    ROBOT_RESTORED_ATTRIBUTES("RobotRestoredAttributes"),
    ROBOT_UPGRADED("RobotUpgraded"),
    ERROR("error"),
    UNKNOWN("UNKNOWN");

    val isRobotRelated: Boolean
        get() = (this == ROBOT_HEALTH_UPDATED || this == ROBOT_MOVED || this == ROBOT_REGENERATED || this == ROBOT_RESOURCE_MINED
                || this ==  ROBOT_RESOURCE_REMOVED || this == ROBOT_RESTORED_ATTRIBUTES || this == ROBOT_UPGRADED
                || this ==  ROBOT_SPAWNED)

    val isPlayerRelated: Boolean
        get() = (this == BANK_INITIALIZED || this == BANK_CLEARED || this == BANK_ACCOUNT_TRANSACTION_BOOKED)

    val isPlanetRelated: Boolean
        get() = (this == RESOURCE_MINED || this == PLANET_DISCOVERED)

    val isNoCategoryYet: Boolean
        get() = (this == ERROR || this == UNKNOWN || this == ROBOT_ATTACKED || this == ROBOTS_REVEALED
                 || this == ROUND_STATUS || this == GAME_STATUS)
    companion object {
        @JvmStatic
        fun findByStringValue(stringValue: String): EventType {
            return Arrays.stream(entries.toTypedArray()).filter { value: EventType -> value.stringValue == stringValue }
                .findFirst()
                .orElse(UNKNOWN)
        }
    }
}