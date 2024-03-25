package de.fiereu.filter

import de.fiereu.CacheEntry
import de.fiereu.FastFilterArrayList
import de.fiereu.PokemonDataManager
import de.fiereu.pokemmo.headless.game.EggGroup
import de.fiereu.pokemmo.headless.game.PokemonEffect

class ParticleFilter : IPokemonFilter {

  override fun getID(): FilterType {
    return FilterType.GENDER_EGG_PARTICLE
  }
  
  override fun filter(cache: FastFilterArrayList<CacheEntry>): List<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>> {
    val final = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()
    val newEntries = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()
    
    final.add(Pair(hashMapOf("male" to true), cache.fastFilter { PokemonDataManager.isMale(it.pokemon) }))
    final.add(Pair(hashMapOf("female" to true), cache.fastFilter { PokemonDataManager.isFemale(it.pokemon) }))
    final.add(Pair(hashMapOf("genderless" to true), cache.fastFilter { PokemonDataManager.isGenderless(it.pokemon) }))

    EggGroup.onlyEggGroups().toList().parallelStream().forEach { eggGroup ->
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

    PokemonEffect.selectableEffects().toList().parallelStream().forEach { particle ->
      val particlePokemon = cache.fastFilter { it.pokemon.selectedEffect == particle }
      for (pair in final) {
        val newMap = pair.first.toMutableMap()
        newMap["particle"] = particle.id
        newEntries.add(
          Pair(
            newMap,
            pair.second.fastFilter { particlePokemon.contains(it) }
          )
        )
      }
    }
    final.clear()
    final.addAll(newEntries)

    return final
  }
}
