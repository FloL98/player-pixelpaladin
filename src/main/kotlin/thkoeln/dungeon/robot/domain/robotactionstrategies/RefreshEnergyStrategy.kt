package thkoeln.dungeon.robot.domain.robotactionstrategies


import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy


class RefreshEnergyStrategy (val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy {
    override fun getCommand(): Command? {
        if(robot.energy < 3)
            return Command().createRegenerateCommand(player.playerId!!, robot.robotId)
        return null
    }
}