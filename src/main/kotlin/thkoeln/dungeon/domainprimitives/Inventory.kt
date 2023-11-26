package thkoeln.dungeon.domainprimitives


import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded


/**
 * Domain Primitive to represent the inventory of a robot
 */
@Embeddable
class Inventory(
    val maxStorage: Int,
    @Embedded
    val resources: InventoryResource,
    val storageLevel: Int,
) {

    fun fromNewResource(resources: InventoryResource): Inventory{
        return fromMaxStorageAndAmountsAndStorageLevel(this.maxStorage, resources, this.storageLevel)
    }


    @get:JsonIgnore
    val usedStorage: Int
        get() = resources.totalResourceAmount
    @get:JsonIgnore
    val full: Boolean
        get() = maxStorage - usedStorage <= 0




    companion object {
        @JvmStatic
        fun fromMaxStorageAndAmountsAndStorageLevel(maxStorage: Int, amounts: InventoryResource, storageLevel: Int): Inventory {
            if (maxStorage < 0) throw DomainPrimitiveException("MaxStorage must be >= 0!")
            if(storageLevel<0) throw DomainPrimitiveException("Storagelevel must be >= 0!")
            if(maxStorage < (amounts.totalResourceAmount)){
                throw DomainPrimitiveException("UsedStorage cannot exceed maxStorage!")
            }
            return Inventory(maxStorage, amounts, storageLevel)
        }

        @JvmStatic
        fun emptyInventory():Inventory{
            return Inventory(0, InventoryResource.emptyResource(),0)

        }
    }
}