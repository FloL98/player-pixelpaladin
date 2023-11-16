package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.CommandType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy


class MoveStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): AbstractRobotActionStrategy() {


    override fun getCommand(): Command? {
        var planetToMoveTo = gameWorld.robotApplicationService.findPlanetToMoveTo(robot)
        return if(planetToMoveTo!= null) {
            //gameWorld.planetDomainService.visitPlanet(planetToMoveTo.planetId)
            Command().createMoveCommand(robot.robotId, planetToMoveTo.planetId, player.playerId!!)
            //gameWorld.robotApplicationService.createCommand(robot, player, CommandType.MOVEMENT, planetToMoveTo.planetId, null, "", 0)
        } else
            null
    }

}