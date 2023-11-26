package thkoeln.dungeon.domainprimitives

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Embeddable


/**
 * Domain Primitive to represent the resources of an inventory
 */
@Embeddable
class InventoryResource(
    @JsonProperty("COAL")
    val coal: Int,
    @JsonProperty("IRON")
    val iron: Int,
    @JsonProperty("GEM")
    val gem: Int,
    @JsonProperty("GOLD")
    val gold: Int,
    @JsonProperty("PLATIN")
    val platin: Int,
    ) {
    @get:JsonIgnore
    val totalResourceAmount: Int
        get() = coal + iron + gem + gold + platin



    fun addFromTypeAndAmount(type: MineableResourceType, amount: Int): InventoryResource{
        return when(type) {
            MineableResourceType.COAL -> fromAmounts(this.coal + amount, this.iron, this.gem, this.gold, this.platin)
            MineableResourceType.IRON -> fromAmounts(this.coal, this.iron + amount, this.gem, this.gold, this.platin)
            MineableResourceType.GEM -> fromAmounts(this.coal, this.iron, this.gem + amount, this.gold, this.platin)
            MineableResourceType.GOLD -> fromAmounts(this.coal, this.iron, this.gem, this.gold + amount, this.platin)
            MineableResourceType.PLATIN -> fromAmounts(this.coal, this.iron, this.gem, this.gold, this.platin + amount)
        }
    }

    fun removeFromTypeAndAmount(type: MineableResourceType, amount: Int): InventoryResource{
        return when(type){
            MineableResourceType.COAL -> fromAmounts(this.coal-amount, this.iron, this.gem,this.gold, this.platin)
            MineableResourceType.IRON -> fromAmounts(this.coal, this.iron-amount, this.gem,this.gold, this.platin)
            MineableResourceType.GEM -> fromAmounts(this.coal, this.iron, this.gem-amount,this.gold, this.platin)
            MineableResourceType.GOLD -> fromAmounts(this.coal, this.iron, this.gem,this.gold-amount, this.platin)
            MineableResourceType.PLATIN -> fromAmounts(this.coal, this.iron, this.gem,this.gold, this.platin-amount)
        }
    }

    companion object {
        @JvmStatic
        fun fromAmounts( coal:Int, iron: Int, gem: Int, gold:Int, platin: Int): InventoryResource {
            if (coal < 0 ||iron < 0 || gem < 0 || gold < 0 || platin < 0)
                throw DomainPrimitiveException("MineableResourceAmount must be >= 0!")
            return InventoryResource(coal, iron, gem, gold, platin)
        }

        @JvmStatic
        fun emptyResource(): InventoryResource{
             return InventoryResource(0, 0, 0, 0, 0)
        }
    }


}