package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy

class FullInventoryStrategy (val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy{


    override fun getCommand(): Command? {
        return Command().createSellingCommand(player.playerId!!,robot.robotId)
    }

    override fun getCommand1(): Command? {
        return if(robot.inventory.full)
            Command().createSellingCommand(player.playerId!!,robot.robotId)
        else null
    }
}