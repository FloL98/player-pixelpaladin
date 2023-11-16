package thkoeln.dungeon.eventlistener.concreteevents.eventdtos

import com.fasterxml.jackson.annotation.JsonAlias

import thkoeln.dungeon.domainprimitives.MineableResourceType


data class MineableResourceDto (
    @JsonAlias("type")
    var resourceType: MineableResourceType?,
    var currentAmount: Int?,
    var maxAmount: Int?
){

}