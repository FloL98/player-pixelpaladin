package thkoeln.dungeon.strategy.domain

import thkoeln.dungeon.game.application.GameApplicationService
import thkoeln.dungeon.planet.domain.PlanetDomainService
import thkoeln.dungeon.robot.application.RobotApplicationService
import thkoeln.dungeon.robot.application.RobotEventHandleService
import thkoeln.dungeon.strategy.application.StrategyService

class GameWorld(val strategyService: StrategyService, val robotApplicationService: RobotApplicationService,
                val planetDomainService: PlanetDomainService, val robotEventHandleService: RobotEventHandleService
) {


}