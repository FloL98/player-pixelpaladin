package thkoeln.dungeon.eventlistener.concreteevents.eventdtos


import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import thkoeln.dungeon.domainprimitives.Inventory
import thkoeln.dungeon.eventlistener.concreteevents.eventdtos.PlanetShortDto
import java.util.*


data class RobotDto(
    @JsonProperty("id")
    val robotId: UUID,
    val player: UUID,
    @JsonProperty("planet")
    @JsonIgnore
    val planetShortDto: PlanetShortDto,
    val alive: Boolean,
    val maxHealth: Int,
    val maxEnergy: Int,
    val energyRegen: Int,
    val attackDamage: Int,
    val miningSpeed: Int,
    val health: Int,
    val energy: Int,
    val healthLevel: Int,
    val damageLevel: Int,
    val miningSpeedLevel: Int,
    val miningLevel: Int,
    val energyLevel: Int,
    val energyRegenLevel: Int,
    val inventory: Inventory,
    ) {


}