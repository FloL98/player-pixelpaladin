package thkoeln.dungeon.domainprimitives


import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded


/**
 * Domain Primitive to represent the inventory of a robot
 */
@Embeddable
class Inventory {

    var maxStorage: Int = 0
    @get:JsonIgnore
    val usedStorage: Int
        get() = resources.totalResourceAmount
    @get:JsonIgnore
    val full: Boolean
        get() = maxStorage - usedStorage <= 0
    var storageLevel: Int = 0
    @Embedded
    var resources: InventoryResource = InventoryResource.emptyResource()

    private constructor(maxStorage: Int, resources: InventoryResource){
        this.maxStorage = maxStorage
        this.resources = resources
    }

    constructor()

    companion object {
        @JvmStatic
        fun fromMaxStorageAndAmounts(maxStorage: Int, amounts: InventoryResource): Inventory {
            if (maxStorage < 0) throw DomainPrimitiveException("MaxStorage must be >= 0!")
            if(maxStorage < (amounts.totalResourceAmount)){
                throw DomainPrimitiveException("UsedStorage cannot exceed maxStorage!")
            }
            return Inventory(maxStorage, amounts)
        }

        @JvmStatic
        fun emptyInventory():Inventory{
            return Inventory(0, InventoryResource.emptyResource())

        }
    }
}