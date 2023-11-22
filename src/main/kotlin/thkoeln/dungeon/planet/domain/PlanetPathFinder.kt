package thkoeln.dungeon.planet.domain

import thkoeln.dungeon.domainprimitives.CompassDirection
import thkoeln.dungeon.domainprimitives.MineableResourceType
import java.util.*
import kotlin.collections.ArrayList

class PlanetPathFinder {



    /*class Graph {
        private val planets = mutableMapOf<Planet, MutableList<Pair<Planet, Int>>>()

        fun addPlanet(planet: Planet) {
            planets[planet] = mutableListOf()
        }

        fun addConnection(planet1: Planet, planet2: Planet, distance: Int) {
            planets[planet1]?.add(planet2 to distance)
            planets[planet2]?.add(planet1 to distance)
        }

        fun shortestPath(start: Planet, targetType: MineableResourceType): List<Planet>? {
            val distances = mutableMapOf<Planet, Int>().apply { this[start] = 0 }
            val predecessors = mutableMapOf<Planet, Planet?>()
            val priorityQueue = PriorityQueue<Planet>(compareBy { distances.getOrDefault(it, Int.MAX_VALUE) }).apply { add(start) }

            while (priorityQueue.isNotEmpty()) {
                val current = priorityQueue.poll()

                planets[current]?.forEach { (neighbor, distance) ->
                    val totalDistance = distances[current]!! + distance
                    if (totalDistance < distances.getOrDefault(neighbor, Int.MAX_VALUE)) {
                        distances[neighbor] = totalDistance
                        predecessors[neighbor] = current
                        priorityQueue.add(neighbor)
                    }
                }
            }

            // Reconstruct the path
            val path = mutableListOf<Planet>()
            var currentPlanet: Planet? = predecessors.filterKeys { it._resourceType == targetType }.minByOrNull { distances[it]!! }?.key
            while (currentPlanet != null) {
                path.add(currentPlanet)
                currentPlanet = predecessors[currentPlanet]
            }

            return if (path.isNotEmpty()) path.reversed() else null
        }
    }

    fun buildPlanetGraph(planets: List<Planet>): Graph{
        val graph = Graph()
        for(planet in planets){
            graph.addPlanet(planet)
        }
        for(planet in planets){
            for(direction in CompassDirection.entries){
                if(planet.getNeighbour(direction)!=null)
                    graph.addConnection(planet, planet.getNeighbour(direction)!!,planet.movementDifficulty.difficulty)
            }
        }
        return graph
    }*/



    class ShortestPathCalculator(var planets: List<Planet>) {

        private val graph: Map<UUID, List<Planet>> = planets.groupBy { it.planetId }

        fun shortestPath(start: Planet, targetType: MineableResourceType): List<Planet>? {
            val distances = mutableMapOf(start.planetId to 0)
            val previous = mutableMapOf<UUID, Planet>()
            val unvisited = planets.filter { it != start }.toMutableList()

            while (unvisited.isNotEmpty()) {
                val current = unvisited.minByOrNull { distances.getOrDefault(it.planetId, Int.MAX_VALUE) } ?: break
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
        }
    }

}