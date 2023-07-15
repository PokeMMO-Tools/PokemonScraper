package de.fiereu

import com.google.gson.Gson
import de.fiereu.filter.*
import de.fiereu.pokemmo.headless.game.Pokemon
import org.slf4j.LoggerFactory

data class CacheEntry(val price: Int, val pokemon: Pokemon)

object PokemonCache {
  private val logger = LoggerFactory.getLogger(PokemonCache::class.java)
  private val entries: FastFilterArrayList<CacheEntry> = FastFilterArrayList()
  private val filters =
      arrayOf(
          ParticleFilter(),
          DittoFilter(3),
          GlitchedOTFilter(),
          BreederFilter(3),
          ShinyFilter(),
          SecretShinyFilter(),
          AlphaFilter(),
      )

  fun addEntry(entry: CacheEntry) {
    entries.add(entry)
  }

  fun persist(database: Database): Int {
    logger.info("Persisting cache")
    val snapshotID = database.createSnapshot()
    if (snapshotID == -1) {
      logger.error("Failed to create snapshot")
      return -1
    }

    // different to the ItemScraper we need to apply filters here and save the result of the filter
    for (filter in filters) {
      // if second of pair is empty, we don't need to save it
      val start = System.currentTimeMillis()
      val filteredEntries = filter.filter(entries).filter { it.second.isNotEmpty() }
      logger.debug("Filter {} took {}ms", filter.getID(), System.currentTimeMillis() - start)
      // get lowest price per pair
      for (entry in filteredEntries) {
        val lowestPrice = entry.second.minByOrNull { it.price }!!.price
        val averagePrice = entry.second.sumOf { it.price.toLong() } / entry.second.size
        val highestPrice = entry.second.maxByOrNull { it.price }!!.price
        val totalEntries = entry.second.size
        val filterConfigJson = Gson().toJson(entry.first)
        database.addEntry(
            snapshotID,
            filter.getID(),
            filterConfigJson,
            lowestPrice,
            averagePrice.toInt(),
            highestPrice,
            totalEntries)
      }
    }

    return snapshotID
  }
}
