package de.fiereu.filter

import de.fiereu.CacheEntry
import de.fiereu.FastFilterArrayList

class GlitchedOTFilter: IPokemonFilter {
    override fun getID(): FilterType {
        return FilterType.GLITCHED_OT
    }

    override fun filter(cache: FastFilterArrayList<CacheEntry>): List<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>> {
        val final = FastFilterArrayList<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>()

        final.add(Pair(hashMapOf("glitchedOT" to true), cache.fastFilter { it.pokemon.ot.isBlank() }))

        return final
    }
}