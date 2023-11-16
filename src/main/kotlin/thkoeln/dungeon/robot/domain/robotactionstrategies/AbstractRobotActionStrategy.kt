package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.domainprimitives.Command



abstract class AbstractRobotActionStrategy {

    abstract fun getCommand():Command?

}