package thkoeln.dungeon.planet.domain

import thkoeln.dungeon.domainprimitives.CompassDirection
import thkoeln.dungeon.domainprimitives.MineableResourceType
import thkoeln.dungeon.robot.domain.Robot
import kotlin.collections.ArrayList

class ShortestPathCalculator(var planets: List<Planet>) {


    //Breadth-First Search (BFS)
    fun findShortestPath(start: Planet, end: Planet): List<Planet>? {
        val visited = mutableSetOf<Planet>()
        val queue = mutableListOf<List<Planet>>()

        queue.add(listOf(start))
        visited.add(start)

        while (queue.isNotEmpty()) {
            val path = queue.removeAt(0)
            val lastPlanet = path.last()

            if (lastPlanet == end) {
                return path
            }

            val neighboursOfLastPlanet = ArrayList<Planet>()
            for(direction in CompassDirection.entries) {
                if(lastPlanet.getNeighbour(direction) != null)
                    neighboursOfLastPlanet.add(lastPlanet.getNeighbour(direction)!!)
            }

            for (neighbor in neighboursOfLastPlanet) {
                if (neighbor !in visited) {
                    val newPath = path.toMutableList()
                    newPath.add(neighbor)
                    queue.add(newPath)
                    visited.add(neighbor)
                }
            }
        }

        return null // No path found
    }

    // Breadth-First Search (BFS)
    fun findShortestPathToType(start: Planet, targetType: MineableResourceType): List<Planet>? {
        val visited = mutableSetOf<Planet>()
        val queue = mutableListOf<List<Planet>>()

        queue.add(listOf(start))
        visited.add(start)

        while (queue.isNotEmpty()) {
            val path = queue.removeAt(0)
            val lastPlanet = path.last()

            if (lastPlanet._resourceType == targetType) {
                return path
            }

            val neighboursOfLastPlanet = ArrayList<Planet>()
            for(direction in CompassDirection.entries) {
                if(lastPlanet.getNeighbour(direction) != null)
                    neighboursOfLastPlanet.add(lastPlanet.getNeighbour(direction)!!)
            }

            for (neighbor in neighboursOfLastPlanet) {
                if (neighbor !in visited) {
                    val newPath = path.toMutableList()
                    newPath.add(neighbor)
                    queue.add(newPath)
                    visited.add(neighbor)
                }
            }
        }

        return null
    }


    //Breadth-First Search (BFS)
    fun findShortestPathToEnemy(start: Planet, enemyList: List<Robot>): List<Planet>? {
        val visited = mutableSetOf<Planet>()
        val queue = mutableListOf<List<Planet>>()

        val enemyPlanets = enemyList.map { it.planet }

        queue.add(listOf(start))
        visited.add(start)

        while (queue.isNotEmpty()) {
            val path = queue.removeAt(0)
            val lastPlanet = path.last()

            if (enemyPlanets.contains(lastPlanet)) {
                return path
            }

            for (neighbor in lastPlanet.getAllNeighborsAsList()) {
                if (neighbor !in visited) {
                    val newPath = path.toMutableList()
                    newPath.add(neighbor)
                    queue.add(newPath)
                    visited.add(neighbor)
                }
            }
        }

        return null
    }

}