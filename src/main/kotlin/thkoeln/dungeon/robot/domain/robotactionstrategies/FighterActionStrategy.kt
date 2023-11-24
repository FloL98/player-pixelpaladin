package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.UpgradeType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy


class FighterActionStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy {


    override fun getCommand(): Command? {
        val enemyFound = gameWorld.robotApplicationService.getAllEnemyRobots().filter { it.planetId == robot.planet.planetId }
        if(enemyFound.isNotEmpty() && robot.energy >= 1)
            return AttackEnemyStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, enemyFound[0].robotId).getCommand()
        else if(robot.energy < robot.planet.movementDifficulty.difficulty)
            return RefreshEnergyStrategy(gameWorld, this.game, this.robot, this.player, this.strategy).getCommand()
        else if(robot.healthLevel < strategy.currentFighterMaxLevels && this.strategy.budgetForFightingUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.HEALTH)?.amount!!)
            return UpgradeStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, UpgradeType.HEALTH).getCommand()
        else if(robot.damageLevel < strategy.currentMinerMaxLevels && this.strategy.budgetForFightingUpgrades.amount >= gameWorld.robotApplicationService.moneyNeededForNextUpgrade(robot,game,UpgradeType.DAMAGE)?.amount!!)
            return UpgradeStrategy(gameWorld, this.game, this.robot, this.player, this.strategy, UpgradeType.DAMAGE).getCommand()

        return MoveStrategy(gameWorld, this.game, this.robot, this.player, this.strategy).getCommand()
    }

    override fun getCommand1(): Command? {
        TODO("Not yet implemented")
    }




}