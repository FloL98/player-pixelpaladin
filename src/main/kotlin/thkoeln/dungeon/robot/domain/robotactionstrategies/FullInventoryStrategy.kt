package thkoeln.dungeon.robot.domain.robotactionstrategies

import org.slf4j.LoggerFactory
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy

class FullInventoryStrategy (val game: Game, val robot: Robot, val player: Player, val strategy: Strategy): RobotActionStrategy{

    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)

    override fun getCommand(): Command? {
        if(robot.inventory.full) {
            return Command().createSellingCommand(player.playerId!!, robot.robotId)
        }
        else return null
    }
}