package thkoeln.dungeon.robot.domain.robotactionstrategies

import org.slf4j.LoggerFactory
import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy


class MinerUpgradeStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy {

    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)


    override fun getCommand(): Command? {
        val upgradeType: UpgradeType =  if(robot.job.minesWhichType()?.neededMiningLevel()!! > robot.miningLevel &&
            gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game, UpgradeType.MINING)?.amount!! <= strategy.budgetForMiningUpgrades.amount)
            UpgradeType.MINING
        else if (this.strategy.budgetForMiningUpgrades.amount >= 50 && robot.miningSpeedLevel == 0)
            UpgradeType.MINING_SPEED
        else if(robot.inventory.storageLevel ==0 && this.strategy.budgetForMiningUpgrades.amount >= 50)
            UpgradeType.STORAGE
        else if(this.strategy.budgetForMiningUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.MINING_SPEED)?.amount!!
            && robot.miningSpeedLevel < strategy.currentMinerMaxLevels && gameWorld.robotApplicationService.checkIfXPercentHaveUpgradeTypeLevelOrHigher(robot.miningSpeedLevel, 90.0F, UpgradeType.MINING_SPEED))
            UpgradeType.MINING_SPEED
        else
            return null
        val itemName =  gameWorld.robotApplicationService.getNextUpgradeLevelAsString(robot, upgradeType)
        val shopItem = this.game.shop.filter { it.name == itemName}
        gameWorld.strategyService.subtractFromMiningBudget(this.strategy, shopItem.first().price.amount)
        return Command().createUpgradePurchaseCommand(player.playerId!!,robot.robotId,shopItem.first())
    }

}