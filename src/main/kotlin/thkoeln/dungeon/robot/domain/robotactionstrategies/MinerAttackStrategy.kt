package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.strategy.domain.Strategy

class MinerAttackStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy {

    override fun getCommand(): Command? {
        val enemyFound = gameWorld.robotApplicationService.getAllEnemyRobots().filter { it.planetId == robot.planet.planetId }
        return if(enemyFound.size == 1 && robot.energy >= 1)
            Command().createBattleCommand(player.playerId!!, robot.robotId, enemyFound[0].robotId)
        else null
    }


}