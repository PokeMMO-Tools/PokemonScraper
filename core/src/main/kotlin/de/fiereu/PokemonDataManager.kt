package de.fiereu

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.fiereu.pokemmo.headless.game.EggGroup
import de.fiereu.pokemmo.headless.game.Pokemon
import org.slf4j.LoggerFactory

object PokemonDataManager {
  private val logger = LoggerFactory.getLogger(PokemonDataManager::class.java)
  private val pokemonData: JsonObject =
    JsonParser.parseReader(
      PokemonCache::class.java.getResourceAsStream("/json/pokemonData.json").reader())
      .asJsonObject
  
  fun getPokemonsByEggGroup(eggGroup: EggGroup): Array<Short> {
    val pokemon = ArrayList<Short>()
    for (entry in pokemonData.entrySet()) {
      if (entry.value.asJsonObject["eggGroup1"].asInt == eggGroup.id || entry.value.asJsonObject["eggGroup2"].asInt == eggGroup.id) {
        pokemon.add(entry.key.toShort())
      }
    }
    return pokemon.toTypedArray()
  }
  
  fun getGenderRatio(pokemonID: Short): Int {
    return pokemonData[pokemonID.toString()].asJsonObject["gender"].asInt
  }
  
  fun isMale(pokemon: Pokemon): Boolean {
    val genderRatio = getGenderRatio(pokemon.id)
    if (isGenderless(pokemon)) return false
    return pokemon.seed and 0xFF >= genderRatio
  }
  
  fun isFemale(pokemon: Pokemon): Boolean {
    val genderRatio = getGenderRatio(pokemon.id)
    if (isGenderless(pokemon)) return false
    return pokemon.seed and 0xFF < genderRatio
  }
  
  fun isGenderless(pokemon: Pokemon): Boolean {
    val genderRatio = getGenderRatio(pokemon.id)
    return genderRatio == 255
  }
  
}