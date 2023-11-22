package thkoeln.dungeon.robot.domain.robotactionstrategies

import org.slf4j.LoggerFactory
import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.CommandType
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy


class UpgradeStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy, val upgradeType: UpgradeType): AbstractRobotActionStrategy() {

    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)

     override fun getCommand(): Command? {
         val itemName =  gameWorld.robotApplicationService.getNextUpgradeLevelAsString(robot, this.upgradeType)
         val shopItem = this.game.shop.filter { it.name == itemName}
         if(upgradeType.isFarmingOriented())
            gameWorld.strategyService.subtractFromMiningBudget(this.strategy, shopItem.first().price.amount)
         else
             gameWorld.strategyService.subtractFromFightingBudget(this.strategy, shopItem.first().price.amount)
         //return  gameWorld.robotApplicationService.createCommand(robot, player, CommandType.BUYING, null, null, itemName,1 )
         return Command().createUpgradePurchaseCommand(player.playerId!!,robot.robotId,shopItem.first())

    }

}