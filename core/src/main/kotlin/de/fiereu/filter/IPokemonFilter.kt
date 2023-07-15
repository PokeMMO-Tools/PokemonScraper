package de.fiereu.filter

import de.fiereu.CacheEntry
import de.fiereu.FastFilterArrayList

interface IPokemonFilter {
  fun getID(): FilterType
  fun filter(cache: FastFilterArrayList<CacheEntry>): List<Pair<MutableMap<String, Any>, FastFilterArrayList<CacheEntry>>>
}