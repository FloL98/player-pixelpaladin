package thkoeln.dungeon.domainprimitives


import jakarta.persistence.Embeddable
import java.util.UUID


//toDo() unused up for delete
/**
 * Domain Primitive to represent a command object
 */
@Embeddable
class CommandObject {
    var robotId: UUID? = null

    var planetId: UUID? = null

    var targetId: UUID? = null

    var itemName: String? = null

    var itemQuantity: Int? = null


    constructor()
    constructor(robotId: UUID?, planetId: UUID?, targetId: UUID?, itemName: String?, itemQuantity: Int?) : this() {
        this.robotId = robotId
        this.planetId = planetId
        this.targetId = targetId
        this.itemName = itemName
        this.itemQuantity = itemQuantity

    }
}