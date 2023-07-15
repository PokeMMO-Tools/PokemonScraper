package de.fiereu

import de.fiereu.filter.FilterType
import org.slf4j.LoggerFactory
import java.sql.Date
import java.sql.DriverManager
import java.sql.Statement
import java.time.ZoneId
import java.time.ZonedDateTime

class Database(private val config: DatabaseConfig) {
  private val logger = LoggerFactory.getLogger(Database::class.java)
  private val connection =
      DriverManager.getConnection(
          "jdbc:mysql://${config.host}:${config.port}/${config.database}",
          config.username,
          config.password)
  
  fun createSnapshot(): Int {
    connection.prepareStatement("INSERT INTO pokemon_snapshots (id, created_at, created_at_epoch) VALUES (NULL, ?, ?)", Statement.RETURN_GENERATED_KEYS)
      .apply {
        val instant = ZonedDateTime.now(ZoneId.systemDefault()).toInstant()
        setDate(1, Date(instant.toEpochMilli()))
        setInt(2, instant.epochSecond.toInt())
        executeUpdate()
      }
      .generatedKeys
      .use {
        return if(it.next()) {
          it.getInt(1)
        } else {
          -1
        }
      }
  }
  
  fun addEntry(
    snapshotID: Int,
    filterID: FilterType,
    filterConfigJson: String,
    lowestPrice: Int,
    averagePrice: Int,
    highestPrice: Int,
    totalEntries: Int,
  ) {
    connection.prepareStatement("INSERT IGNORE INTO pokemon_entries (snapshot_id, filter_id, filter_config, min_price, avg_price, max_price, total_entries) VALUES (?, ?, ?, ?, ?, ?, ?)")
      .apply {
        setInt(1, snapshotID)
        setInt(2, filterID.ordinal)
        setString(3, filterConfigJson)
        setInt(4, lowestPrice)
        setInt(5, averagePrice)
        setInt(6, highestPrice)
        setInt(7, totalEntries)
        executeUpdate()
      }
  }
}
