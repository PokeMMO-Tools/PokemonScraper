package de.fiereu.filter

import de.fiereu.CacheEntry
import de.fiereu.FastFilterArrayList
import de.fiereu.PokemonDataManager
import de.fiereu.pokemmo.headless.game.Nature
import de.fiereu.pokemmo.headless.game.Rarity

class ShinyFilter: IPokemonFilter {
  override fun getID(): FilterType {
    return FilterType.SHINY_GENDER_STATS_NATURE
  }

  override fun filter(cache: FastFilterArrayList<CacheEntry>): List<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>> {
    val final = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()
    val newEntries = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()

    final.add(Pair(hashMapOf<String, Any>("shiny" to true), cache.fastFilter { it.pokemon.getRarity(Rarity.SHINY) }))

    for (pair in final) {
      val newMap = pair.first.toMutableMap()
      newMap["male"] = true
      newEntries.add(
        Pair(
          newMap,
          pair.second.fastFilter { PokemonDataManager.isMale(it.pokemon) }
        )
      )
    }
    for (pair in final) {
      val newMap = pair.first.toMutableMap()
      newMap["male"] = false
      newEntries.add(
        Pair(
          newMap,
          pair.second.fastFilter { PokemonDataManager.isFemale(it.pokemon) }
        )
      )
    }
    final.clear()
    final.addAll(newEntries)
    newEntries.clear()

    for (statCombination in FilterUtil.getAllStatCombinations(3)) {
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
    newEntries.clear()

    for (nature in Nature.values()) {
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

    return final
  }
}