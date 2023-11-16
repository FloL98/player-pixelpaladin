package thkoeln.dungeon.eventlistener.concreteevents

import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.eventlistener.AbstractEvent
import thkoeln.dungeon.robot.domain.Robot
import java.util.*
import thkoeln.dungeon.domainprimitives.UpgradeType

data class RobotUpgradedEvent(
    var robotId: UUID,
    var level: Int,
    @JsonProperty("upgrade")
    var upgradeType: UpgradeType,
    var robot: Robot
): AbstractEvent() {

    override val isValid: Boolean
        get() = level >= 0
}