package thkoeln.dungeon.domainprimitives




import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.util.*


/**
 * Domain Primitive to represent a command that a player can send
 */
@Embeddable
class Command {

    var playerId: UUID = UUID.randomUUID()

    @JsonProperty("type")
    var commandType: String = ""

    @Embedded
    @JsonProperty("data")
    var commandObject: CommandObject = CommandObject()

    constructor()
    constructor(playerId: UUID, commandType: String) : this() {
        this.playerId = playerId
        this.commandType = commandType
    }

    fun createMoveCommand(robotId: UUID, planetId: UUID, playerId: UUID): Command {
        val command = Command(playerId,CommandType.MOVEMENT.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        command.commandObject.planetId = planetId
        return command
    }

    fun createItemPurchaseCommand(playerId: UUID,robotId:UUID, tradableItem: TradableItem, quantity: Int): Command {
        val command = Command(playerId,CommandType.BUYING.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        command.commandObject.itemName = tradableItem.name
        command.commandObject.itemQuantity = quantity
        return command
    }

    fun createUpgradePurchaseCommand(playerId: UUID,robotId:UUID, tradableItem: TradableItem): Command {
        val command = Command(playerId,CommandType.BUYING.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        command.commandObject.itemName = tradableItem.name
        command.commandObject.itemQuantity = 1
        return command
    }

    fun createRobotPurchaseCommand(playerId: UUID, quantity: Int): Command {
        val command = Command(playerId,CommandType.BUYING.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.itemName = TradableType.ROBOT.name
        command.commandObject.itemQuantity = quantity
        return command
    }


    fun createRegenerateCommand(playerId: UUID, robotId: UUID): Command {
        val command = Command(playerId,CommandType.REGENERATE.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        return command
    }

    fun createMiningCommand(playerId: UUID, robotId: UUID): Command {
        val command = Command(playerId,CommandType.MINING.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        return command
    }

    fun createSellingCommand(playerId: UUID, robotId: UUID): Command {
        val command = Command(playerId,CommandType.SELLING.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        return command
    }

    fun createBattleCommand(playerId: UUID, robotId: UUID, targetId: UUID): Command {
        val command = Command(playerId,CommandType.BATTLE.stringValue )
        command.commandObject = CommandObject()
        command.commandObject.robotId = robotId
        command.commandObject.targetId = targetId
        return command
    }

}