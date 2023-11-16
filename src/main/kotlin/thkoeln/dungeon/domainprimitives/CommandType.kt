package thkoeln.dungeon.domainprimitives


/**
 * Domain Primitive to represent a command type
 */
enum class CommandType(var stringValue: String) {
    MOVEMENT("movement"),
    BATTLE("battle"),
    MINING("mining"),
    REGENERATE("regenerate"), //in der open-api steht "regeneration", aber "regenerate" ist richtig
    BUYING("buying"),
    SELLING("selling");




}