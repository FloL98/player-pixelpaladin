package thkoeln.dungeon.domainprimitives

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.persistence.Embeddable




/**
 * Domain Primitive to represent a mineable resource
 */
@Embeddable
class MineableResource (
    //spring jpa doesnt like embeddables with not-nullable types persisted in entities (mineableResource as embedded in Planet)
    //so these attributes have no be nullable, if not i would implement these as not nullable
    @JsonAlias("type")
    val resourceType: MineableResourceType? ,
    val currentAmount: Int? ,
    val maxAmount: Int?
){


    fun decreaseBy(amount: Int): MineableResource{
        if(resourceType==null || currentAmount == null || maxAmount==null)
            throw DomainPrimitiveException("Cannot decrease because attribute is null")
        else if(amount<0)
            throw DomainPrimitiveException("Amount < 0")
        else if(this.currentAmount - amount <0)
            throw DomainPrimitiveException("Amount cannot be decreased under 0")
        return fromTypeAndAmount(this.resourceType, this.currentAmount -amount, this.maxAmount)
    }



    override fun equals(other: Any?): Boolean { //TODO: Wird noch gefixt
        if (this === other) return true
        if (other !is MineableResource) return false
        return this.resourceType == other.resourceType && this.currentAmount == other.currentAmount && this.maxAmount == other.maxAmount
    }

    override fun hashCode(): Int { //TODO: Wird noch gefixt
        var result = resourceType.hashCode() ?: 0
        result = 31 * result + (currentAmount ?: 0)
        result = 31 * result + (maxAmount ?: 0)
        return result
    }

    companion object {
        @JvmStatic
        fun fromTypeAndAmount(mineableResourceType: MineableResourceType, currentAmount: Int, maxAmount: Int): MineableResource {
            if (currentAmount <= 0) throw DomainPrimitiveException("CurrentAmount must be > 0!")
            if(maxAmount < 0 || maxAmount<currentAmount) throw DomainPrimitiveException("MaxAmount must be > 0 and >= currentAmount")
            return MineableResource(mineableResourceType, currentAmount,maxAmount)
        }

    }
}