package thkoeln.dungeon.robot.domain.robotactionstrategies

import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.planet.domain.Planet
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.strategy.domain.Strategy

class FleeStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy {


    // toDO create this strategy
    override fun getCommand(): Command? {
        val planetToMoveTo: Planet?
        if(robot.health < 3)
            planetToMoveTo= gameWorld.robotApplicationService.findPlanetToMoveTo(robot)
        else
            return null

        return if(planetToMoveTo!= null) {
            Command().createMoveCommand(robot.robotId, planetToMoveTo.planetId, player.playerId!!)
        } else
            null
    }

}