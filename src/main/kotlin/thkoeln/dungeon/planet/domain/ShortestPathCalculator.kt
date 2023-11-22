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

            /*val neighboursOfLastPlanet = ArrayList<Planet>()
            for(direction in CompassDirection.entries) {
                if(lastPlanet.getNeighbour(direction) != null)
                    neighboursOfLastPlanet.add(lastPlanet.getNeighbour(direction)!!)
            }*/


            for (neighbor in convertNeighborsToList(lastPlanet)) {
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

    /**
     * this function puts all neighbors/neighbor-attributes of a planet into a single list
     */
    fun convertNeighborsToList(planet: Planet): List<Planet>{
        val neighbors = ArrayList<Planet>()
        for(direction in CompassDirection.entries) {
            if(planet.getNeighbour(direction) != null)
                neighbors.add(planet.getNeighbour(direction)!!)
        }
        return neighbors
    }

    /*private val graph: Map<UUID, List<Planet>> = planets.groupBy { it.planetId }

    //doesnt work yet
    fun shortestPathToResourceType(start: Planet, targetType: MineableResourceType): List<Planet>? {
        val distances = mutableMapOf(start.planetId to 0) //<planetId, distanz zum start>
        val previous = mutableMapOf<UUID, Planet>() // <<UUID vorgänger, Planet vorgänger>
        val unvisited = planets.filter { it != start }.toMutableList()  // list<unbesuchte planeten ohne start>

        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances.getOrDefault(it.planetId, Int.MAX_VALUE) } ?: break //
            unvisited.remove(current)

            for (neighbor in graph.getValue(current.planetId)) {
                val alt = distances.getValue(current.planetId) + 1 // Assuming equal weight for all edges

                if (alt < distances.getOrDefault(neighbor.planetId, Int.MAX_VALUE)) {
                    distances[neighbor.planetId] = alt
                    previous[neighbor.planetId] = current
                }
            }
        }

        val path = mutableListOf<Planet>()
        var current = unvisited.firstOrNull { it._resourceType == targetType } ?: return null

        while (previous.containsKey(current.planetId)) {
            path.add(current)
            current = previous.getValue(current.planetId)
        }

        path.add(start)
        path.reverse()

        return if (path.first() == start) path else null
    }*/

    /*fun shortestPathToString(start: Planet, targetType: MineableResourceType): String{
        val path = shortestPathToResourceType(start,targetType)
        if(path == null)
            return "null - no path found"
        else{
            var string = ""
            for(planet in path){
                string += " ${planet.planetId}"
            }
            return string
        }
    }*/

    fun shortestPathToString(start: Planet, targetType: MineableResourceType): String {
        val path = findShortestPathToType(start, targetType)
        if (path == null)
            return "null - no path found"
        else {
            var string = ""
            for (planet in path) {
                string += " ${planet.planetId}"
            }
            return string
        }
    }
}