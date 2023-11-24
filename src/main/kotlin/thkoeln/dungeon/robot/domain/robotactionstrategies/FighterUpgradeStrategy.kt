package thkoeln.dungeon.robot.domain.robotactionstrategies

import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.strategy.domain.Strategy

class FighterUpgradeStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy {

    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)

    override fun getCommand(): Command? {
        val upgradeType: UpgradeType = if(robot.healthLevel < strategy.currentFighterMaxLevels && this.strategy.budgetForFightingUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.HEALTH)?.amount!!)
            UpgradeType.HEALTH
        else if(robot.damageLevel < strategy.currentFighterMaxLevels && this.strategy.budgetForFightingUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.DAMAGE)?.amount!!)
            UpgradeType.DAMAGE
        else
            return null
        val itemName =  gameWorld.robotApplicationService.getNextUpgradeLevelAsString(robot, upgradeType)
        val shopItem = this.game.shop.filter { it.name == itemName}
        gameWorld.strategyService.subtractFromFightingBudget(this.strategy, shopItem.first().price.amount)
        return Command().createUpgradePurchaseCommand(player.playerId!!,robot.robotId,shopItem.first())
    }

    override fun getCommand1(): Command? {
        val upgradeType: UpgradeType = if(robot.healthLevel < strategy.currentFighterMaxLevels && this.strategy.budgetForFightingUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.HEALTH)?.amount!!)
            UpgradeType.HEALTH
        else if(robot.damageLevel < strategy.currentFighterMaxLevels && this.strategy.budgetForFightingUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.DAMAGE)?.amount!!)
            UpgradeType.DAMAGE
        else
            return null
        val itemName =  gameWorld.robotApplicationService.getNextUpgradeLevelAsString(robot, upgradeType)
        val shopItem = this.game.shop.filter { it.name == itemName}
        gameWorld.strategyService.subtractFromFightingBudget(this.strategy, shopItem.first().price.amount)
        return Command().createUpgradePurchaseCommand(player.playerId!!,robot.robotId,shopItem.first())
    }

}