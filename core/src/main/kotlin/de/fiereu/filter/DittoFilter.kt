package de.fiereu.filter

import de.fiereu.CacheEntry
import de.fiereu.FastFilterArrayList
import de.fiereu.pokemmo.headless.game.Nature

class DittoFilter(
    private val maxStats: Int
): IPokemonFilter {

    override fun getID(): FilterType {
        return FilterType.DITTO
    }

    override fun filter(cache: FastFilterArrayList<CacheEntry>): List<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>> {
        val final = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()
        val newEntries = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()

        final.add(Pair(hashMapOf(), cache.fastFilter { it.pokemon.id == 132.toShort() }))

        Nature.entries.parallelStream().forEach { nature ->
            val naturePokemon = cache.fastFilter { it.pokemon.nature == nature }
            for (pair in final) {
                val newMap = pair.first.toMutableMap()
                newMap["nature"] = nature.ordinal
                newEntries.add(
                    Pair(
                        newMap,
                        pair.second.fastFilter { naturePokemon.contains(it) }
                    )
                )
            }
        }

        final.clear()
        final.addAll(newEntries)
        newEntries.clear()

        FilterUtil.getAllStatCombinations(maxStats).toList().parallelStream().forEach { statCombination ->
            val statPokemon = cache.fastFilter {
                var matches = true
                for (stat in statCombination) {
                    matches = matches && it.pokemon.ivs[stat.ordinal] == 31.toByte()
                }
                matches
            }
            for (pair in final) {
                val newMap = pair.first.toMutableMap()
                newMap["stats"] = statCombination.map { it.ordinal }.toTypedArray()
                newEntries.add(
                    Pair(
                        newMap,
                        pair.second.fastFilter { statPokemon.contains(it) }
                    )
                )
            }
        }

        final.clear()
        final.addAll(newEntries)

        return final
    }
}