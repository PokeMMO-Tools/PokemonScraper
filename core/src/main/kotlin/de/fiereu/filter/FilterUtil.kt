package de.fiereu.filter

import de.fiereu.pokemmo.headless.game.PokemonStats

object FilterUtil {
    fun getAllStatCombinations(max: Int): Array<Array<PokemonStats>> {
        val stats = PokemonStats.valuesVisible()
        val combinations = arrayListOf<Array<PokemonStats>>()
        val maxMask = (1 shl stats.size) - 1
        for (mask in 1..maxMask) {
            if (Integer.bitCount(mask) > max) {
                continue
            }
            val combination = arrayListOf<PokemonStats>()
            for (stat in stats) {
                if (mask and (1 shl stat.ordinal) != 0) {
                    combination.add(stat)
                }
            }
            combinations.add(combination.toTypedArray())
        }
        return combinations.toTypedArray()
    }
}