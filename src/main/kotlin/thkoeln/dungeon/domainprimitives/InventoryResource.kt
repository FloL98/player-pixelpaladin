package thkoeln.dungeon.domainprimitives

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Embeddable


/**
 * Domain Primitive to represent the resources of an inventory
 */
@Embeddable
class InventoryResource {
    @JsonProperty("COAL")
    var coal: Int = 0
    @JsonProperty("IRON")
    var iron: Int = 0
    @JsonProperty("GEM")
    var gem: Int = 0
    @JsonProperty("GOLD")
    var gold: Int = 0
    @JsonProperty("PLATIN")
    var platin: Int = 0

    @get:JsonIgnore
    val totalResourceAmount: Int
        get() = coal + iron + gem + gold + platin

    private constructor(coal:Int, iron: Int, gem: Int, gold:Int, platin: Int){
        this.coal = coal
        this.iron = iron
        this.gem = gem
        this.gold = gold
        this.platin = platin
    }
    constructor()

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