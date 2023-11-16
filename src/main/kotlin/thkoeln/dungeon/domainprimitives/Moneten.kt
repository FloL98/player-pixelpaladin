package thkoeln.dungeon.domainprimitives


import jakarta.persistence.Embeddable



/**
 * Domain Primitive to represent money
 */
@Embeddable
class Moneten() {
    var amount: Int = 0

    private constructor(amount: Int) : this() {
        this.amount = amount
    }

    fun canBuyThatManyFor(price: Moneten): Int {
        if (price.amount <= 0) throw DomainPrimitiveException("price <= 0")
        return amount / price.amount
    }

    fun decreaseBy(amount : Int): Moneten{
        return fromInteger(this.amount-amount)
    }

    fun increaseBy(amount: Int): Moneten{
        return fromInteger(this.amount+amount)
    }


    companion object {
        @JvmStatic
        fun fromInteger(amount: Int): Moneten {
            if (amount < 0) throw DomainPrimitiveException("Amount must be >= 0!")
            return Moneten(amount)
        }
    }

}