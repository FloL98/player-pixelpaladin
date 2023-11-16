package thkoeln.dungeon.player.domain


import thkoeln.dungeon.DungeonPlayerRuntimeException


class CannotRegisterPlayerException(message: String?) : DungeonPlayerRuntimeException(message)