package thkoeln.dungeon.strategy.application


import thkoeln.dungeon.strategy.domain.Policy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import thkoeln.dungeon.domainprimitives.Moneten
import thkoeln.dungeon.game.domain.Game
import thkoeln.dungeon.game.domain.GameRepository
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter
import java.util.*
import thkoeln.dungeon.strategy.domain.Strategy
import thkoeln.dungeon.strategy.domain.StrategyRepository


@Service
class StrategyService@Autowired constructor(
    private val strategyRepository: StrategyRepository,
    private val gameRepository: GameRepository,
){

    private val logger = LoggerFactory.getLogger(GameServiceRESTAdapter::class.java)


    fun createStrategyForGameIfNotExists(gameId: UUID?){
        val game = gameRepository.findByGameId(gameId!!).first()
        if(strategyRepository.findByGame(game).isEmpty) {
            val strategy = Strategy()
            strategy.game = game
            strategyRepository.save(strategy)
        }
    }

    fun getStrategyByGame(game: Game): Strategy {
        return strategyRepository.findByGame(game).orElseThrow{ StrategyException("No strategy found for this game!") }
    }

    fun updateStrategy(game: Game?, balance:Moneten, currentNumberOfRobots: Int?){
        val strategy = strategyRepository.findByGame(game).orElseThrow{ StrategyException("No strategy found for this game!") }
        strategy.totalBalance = balance
        updateMaxNumberOfRobots(strategy)
        updatePolicy(strategy, currentNumberOfRobots!!)
        calculateBudgets(strategy)
        updateNumberOfDifferentRobotsJobs(strategy)
        updateMaxLevels(strategy)
    }



    private fun calculateBudgets(strategy: Strategy){
        strategy.budgetForRobots = Moneten.fromInteger(((strategy.gamePolicy.percentageMoneyForRobots.toFloat()/100)
                * strategy.totalBalance.amount).toInt())
        strategy.budgetForMiningUpgrades = Moneten.fromInteger(((strategy.gamePolicy.percentageMoneyForMiningUpgrades.toFloat()/100)
                * strategy.totalBalance.amount).toInt())
        strategy.budgetForFightingUpgrades = Moneten.fromInteger(((strategy.gamePolicy.percentageMoneyForFightingUpgrades.toFloat()/100)
                * strategy.totalBalance.amount).toInt())
        strategyRepository.save(strategy)
        logger.info("Current budgetForRobots: ${strategy.budgetForRobots.amount}")
        logger.info("Current budgetForMiningUpgrades: ${strategy.budgetForMiningUpgrades.amount}")
        logger.info("Current budgetForForFightingUpgrades: ${strategy.budgetForFightingUpgrades.amount}")
    }

    private fun updatePolicy(strategy: Strategy, currentNumberOfRobots: Int){
        val currentRound = strategy.game?.currentRoundNumber!!
        val maxRounds = strategy.game?.maxRounds!!
        when(currentRound){
            in 0..30 -> strategy.gamePolicy = Policy.EARLY_POOR
            in 30..50-> strategy.gamePolicy = Policy.EARLY_RICH
            in 0..(0.4*maxRounds).toInt()-> strategy.gamePolicy = Policy.MID_POOR
            in (0.4*maxRounds).toInt()..(0.6*maxRounds).toInt()-> strategy.gamePolicy = Policy.MID_RICH
            in (0.6*maxRounds).toInt()..(0.8*maxRounds).toInt()-> strategy.gamePolicy = Policy.LATE_POOR
            in (0.8*maxRounds).toInt() .. maxRounds -> strategy.gamePolicy = Policy.LATE_RICH
            else -> throw StrategyException("Round number cannot be negative!")
        }
        //Policy soll verringert werden, wenn zu wenig Robots da sind, damit mehr Budget für Robots zur Verfügung stehen
        val policyPenalty = calculatePolicyPenalty(strategy.maxNumberOfRobots, currentNumberOfRobots, currentRound)
        strategy.gamePolicy = Policy.fromInt(strategy.gamePolicy.ordinal - policyPenalty)
        strategyRepository.save(strategy)
        logger.info("Lowered policy by: $policyPenalty.($currentNumberOfRobots/${strategy.maxNumberOfRobots}) Robots!")
        logger.info("Current Policy: ${strategy.gamePolicy.name}")
    }

    private fun calculatePolicyPenalty(maxNumberOfRobots: Int, currentNumberOfRobots: Int, currentRound: Int): Int{
        if(currentNumberOfRobots == 0)
            return 5
        /*val quotient = maxNumberOfRobots.toDouble()/currentNumberOfRobots.toDouble()
        if(quotient < 1.2)
            return 0
        return quotient.toInt()*/
        val quotient = (currentRound.toDouble()*0.5) / currentNumberOfRobots.toDouble()
        if(quotient <= 1)
            return 0
        else
            return quotient.toInt()
    }

    //aktuell keine dynamische anpassung, sondern flat 150 damit player nicht crasht
    private fun updateMaxNumberOfRobots(strategy: Strategy){
        var currentRound = strategy.game?.currentRoundNumber!!
        //strategy.maxNumberOfRobots = 5 + (currentRound/2)
        strategy.maxNumberOfRobots = 150
        //if(strategy.maxNumberOfRobots> 150)
            //strategy.maxNumberOfRobots = 150
        strategyRepository.save(strategy)
    }



    fun subtractFromMiningBudget(strategy: Strategy, amount: Int){
        strategy.budgetForMiningUpgrades = Moneten.fromInteger(strategy.budgetForMiningUpgrades.amount -amount)
        strategyRepository.save(strategy)
    }
    fun subtractFromFightingBudget(strategy: Strategy, amount: Int){
        strategy.budgetForFightingUpgrades = Moneten.fromInteger(strategy.budgetForFightingUpgrades.amount -amount)
        strategyRepository.save(strategy)
    }

    private fun updateNumberOfDifferentRobotsJobs(strategy: Strategy){
        val currentRound = strategy.game?.currentRoundNumber!!
        var maxRounds = strategy.game?.maxRounds!!
        when(currentRound){
            in 0..50 -> {
            }
            in 50..100-> {
                strategy.maxNumberOfIronMiners = (currentRound/10)
            }
            in 100 .. Int.MAX_VALUE -> {
                strategy.maxNumberOfGemMiners = 5+((currentRound - 100) / 6)
                strategy.maxNumberOfFighters = 5+((currentRound - 100) / 6)
                strategy.maxNumberOfIronMiners = 15+ ((currentRound - 100) / 6)
            }
            else -> throw StrategyException("Round number cannot be negative!")
        }
        strategyRepository.save(strategy)
    }



    fun updateMaxLevels(strategy: Strategy) {
        val BUDGET_TIER_1 = 15000
        val BUDGET_TIER_2 = 30000
        val BUDGET_TIER_3 = 70000

        fun updateLevels(budget: Int): Int {
            return when (budget) {
                in 0..BUDGET_TIER_1 -> 2
                in BUDGET_TIER_1 + 1..BUDGET_TIER_2 -> 3
                in BUDGET_TIER_2 + 1..BUDGET_TIER_3 -> 4
                in BUDGET_TIER_3 + 1..Int.MAX_VALUE -> 5
                else -> throw StrategyException("Invalid budget: $budget")
            }
        }
        strategy.currentMinerMaxLevels = updateLevels(strategy.budgetForMiningUpgrades.amount)
        strategy.currentFighterMaxLevels = updateLevels(strategy.budgetForFightingUpgrades.amount)
        strategyRepository.save(strategy)
    }



    /*fun updateMaxLevels(strategy: Strategy){
        when(strategy.budgetForMiningUpgrades.amount){
            in 0..15000 -> strategy.currentMinerMaxLevels = 2
            in 15000..30000 -> strategy.currentMinerMaxLevels = 3
            in 30000 .. 70000 -> strategy.currentMinerMaxLevels = 4
            in 70000 .. Int.MAX_VALUE -> strategy.currentMinerMaxLevels = 5
            else -> throw StrategyException("Budget for mining upgrades cannot be negative!")
        }

        when(strategy.budgetForFightingUpgrades.amount){
            in 0..15000 -> strategy.currentFighterMaxLevels = 2
            in 15000..30000 -> strategy.currentFighterMaxLevels = 3
            in 30000 .. 70000 -> strategy.currentFighterMaxLevels = 4
            in 70000 .. Int.MAX_VALUE -> strategy.currentFighterMaxLevels = 5
            else -> throw StrategyException("Budget for fighting uprgrades cannot be negative!")
        }
        strategyRepository.save(strategy)
    }*/

    fun deleteAllStrategies(){
        strategyRepository.deleteAll()
    }



}