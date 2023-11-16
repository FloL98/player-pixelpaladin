package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy

//toDo strategien ersetzen durch einzelne strategien, die aufgerufen werden und entweder null return und somit die nächste strategy ausprobiert wird
// oder sie returnen einen validen command
class MinerActionStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): AbstractRobotActionStrategy() {


    override fun getCommand(): Command? {
        if(robot.energy < 3)
            return RefreshEnergyStrategy(gameWorld, this.game, this.robot, this.player, this.strategy).getCommand()
        else if(robot.inventory.full) {
            return FullInventoryStrategy(gameWorld, this.game, this.robot, this.player, this.strategy).getCommand()
        }
        else if(gameWorld.robotApplicationService.getAllEnemiesOnPlanet(robot.planet).isNotEmpty()
            && gameWorld.robotApplicationService.getAllEnemiesOnPlanet(robot.planet).first.levels.getSumOfFightingLevels() == 0 ){
            return AttackEnemyStrategy(gameWorld,game, robot, player, strategy,gameWorld.robotApplicationService.getAllEnemiesOnPlanet(robot.planet).first.robotId!!).getCommand()
        }
        else if(robot.job.minesWhichType()?.neededMiningLevel()!! > robot.miningLevel &&
                gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game, UpgradeType.MINING)?.amount!! <= strategy.budgetForMiningUpgrades.amount
        ){
            return UpgradeStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, UpgradeType.MINING).getCommand()
        }
        else if (this.strategy.budgetForMiningUpgrades.amount >= 50 && robot.miningSpeedLevel == 0){
            return UpgradeStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, UpgradeType.MINING_SPEED).getCommand()
        }
        else if(robot.inventory.storageLevel ==0 && this.strategy.budgetForMiningUpgrades.amount >= 50) {
            return UpgradeStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, UpgradeType.STORAGE).getCommand()
        }
        else if(this.strategy.budgetForMiningUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.MINING_SPEED)?.amount!!
            && robot.miningSpeedLevel < strategy.currentMinerMaxLevels && gameWorld.robotApplicationService.checkIfXPercentHaveUpgradeTypeLevelOrHigher(robot.miningSpeedLevel, 90.0F, UpgradeType.MINING_SPEED))
        {
            return UpgradeStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, UpgradeType.MINING_SPEED).getCommand()
        }
        else if (robot.planet.mineableResource != null && robot.planet.mineableResource?.currentAmount!! >0) {
            if(robot.job.minesWhichType() == robot.planet.mineableResource?.resourceType && robot.planet.mineableResource?.resourceType?.neededMiningLevel() == robot.miningLevel  )
                return FarmResourceStrategy(gameWorld, this.game, this.robot, this.player, this.strategy).getCommand()
        }
        return MoveStrategy(gameWorld, this.game, this.robot, this.player, this.strategy).getCommand()
    }

}