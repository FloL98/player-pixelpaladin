package thkoeln.dungeon.robot.domain.robotactionstrategies

import thkoeln.dungeon.domainprimitives.Command

interface RobotActionStrategy {
    fun getCommand(): Command?

    fun getCommand1(): Command?
}