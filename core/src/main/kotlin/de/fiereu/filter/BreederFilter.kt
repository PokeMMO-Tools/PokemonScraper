package de.fiereu.filter

import de.fiereu.CacheEntry
import de.fiereu.FastFilterArrayList
import de.fiereu.PokemonDataManager
import de.fiereu.pokemmo.headless.game.EggGroup
import de.fiereu.pokemmo.headless.game.Nature

class BreederFilter(
  private val maxStats: Int
): IPokemonFilter {
  override fun getID(): FilterType {
    return FilterType.GENDER_EGG_STATS_NATURE
  }

  override fun filter(cache: FastFilterArrayList<CacheEntry>): List<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>> {
    val final = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()
    val newEntries = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()

    final.add(Pair(hashMapOf<String, Any>("male" to true), cache.fastFilter { PokemonDataManager.isMale(it.pokemon) }))
    final.add(Pair(hashMapOf<String, Any>("male" to false), cache.fastFilter { PokemonDataManager.isFemale(it.pokemon) }))

    for (eggGroup in EggGroup.onlyEggGroups()) {
      val eggGroupPokemon = PokemonDataManager.getPokemonsByEggGroup(eggGroup)
      for (pair in final) {
        val newMap = pair.first.toMutableMap()
        newMap["eggGroup"] = eggGroup.id
        newEntries.add(
          Pair(
            newMap,
            pair.second.fastFilter { eggGroupPokemon.contains(it.pokemon.id) }
          )
        )
      }
    }
    final.clear()
    final.addAll(newEntries)
    newEntries.clear()


    for (statCombination in FilterUtil.getAllStatCombinations(maxStats)) {
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