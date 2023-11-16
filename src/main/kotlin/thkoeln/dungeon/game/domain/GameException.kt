package thkoeln.dungeon.game.domain



import thkoeln.dungeon.DungeonPlayerRuntimeException

class GameException(message: String?) : DungeonPlayerRuntimeException(message)