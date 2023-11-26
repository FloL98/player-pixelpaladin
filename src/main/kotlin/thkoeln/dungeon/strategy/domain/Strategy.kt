package thkoeln.dungeon.strategy.domain


import jakarta.persistence.*
import thkoeln.dungeon.domainprimitives.Moneten
import thkoeln.dungeon.game.domain.Game
import java.util.*

@Entity
class Strategy{

    @Id
    val id: UUID = UUID.randomUUID()
    var gamePolicy: Policy = Policy.EARLY_POOR

    @Embedded
    @AttributeOverride(name="amount", column=Column(name="budgetForMiningUpgrades_amount"))
    var budgetForMiningUpgrades: Moneten = Moneten.fromInteger(0)

    @Embedded
    @AttributeOverride(name="amount", column=Column(name="budgetForFightingUpgrades_amount"))
    var budgetForFightingUpgrades: Moneten = Moneten.fromInteger(0)

    @Embedded
    @AttributeOverride(name="amount", column=Column(name="budgetForRobots_amount"))
    var budgetForRobots: Moneten = Moneten.fromInteger(0)

    var maxNumberOfRobots: Int = 150

    @Embedded
    @AttributeOverride(name="amount", column=Column(name="totalBalance_amount"))
    var totalBalance: Moneten = Moneten.fromInteger(0)
    @OneToOne
    var game: Game? = null

    var maxNumberOfFighters: Int = 0
    var maxNumberOfIronMiners: Int = 0
    var maxNumberOfGemMiners: Int = 0

    var currentMinerMaxLevels: Int = 2
    var currentFighterMaxLevels: Int = 2

}