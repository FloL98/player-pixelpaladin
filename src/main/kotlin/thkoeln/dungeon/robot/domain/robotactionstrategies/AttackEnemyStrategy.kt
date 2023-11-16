package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.strategy.domain.GameWorld
import thkoeln.dungeon.domainprimitives.Command
import thkoeln.dungeon.domainprimitives.CommandType
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.player.domain.Player
import thkoeln.dungeon.robot.domain.Robot
import thkoeln.dungeon.strategy.domain.Strategy
import java.util.*

class AttackEnemyStrategy(val gameWorld: GameWorld, val game: Game, val robot: Robot, val player: Player, val strategy: Strategy, val targetId: UUID): AbstractRobotActionStrategy() {

    override fun getCommand(): Command? {
        return Command().createBattleCommand(player.playerId!!, robot.robotId, targetId)
    }

}