package thkoeln.dungeon.domainprimitives


import jakarta.persistence.Embeddable



/**
 * Domain Primitive to represent the movement difficulty of a planet
 */
@Embeddable
class MovementDifficulty() {
    var difficulty: Int = 1

    private constructor(difficulty: Int) : this(){
        this.difficulty = difficulty
    }


    companion object {
        @JvmStatic
        fun fromInteger(difficulty: Int): MovementDifficulty {
            if (difficulty < 1) throw DomainPrimitiveException("Difficulty must be >= 1!")
            if (difficulty > 3) throw DomainPrimitiveException("Difficulty must be <= 3!")
            return MovementDifficulty(difficulty)
        }
    }
}