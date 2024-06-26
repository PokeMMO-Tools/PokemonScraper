package de.fiereu

import com.sksamuel.hoplite.ConfigLoaderBuilder
import de.fiereu.packets.CustomGTLPagePacket
import de.fiereu.pokemmo.headless.Client
import de.fiereu.pokemmo.headless.config.ClientConfig
import de.fiereu.pokemmo.headless.game.Account
import de.fiereu.pokemmo.headless.game.gtl.ListingType
import de.fiereu.pokemmo.headless.network.packets.GamePacketFactory
import de.fiereu.pokemmo.headless.network.packets.game.serializable.RequestGTLPagePacket
import de.fiereu.pokemmo.headless.network.server.ServerType
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

class PokemonScraper(
  private val client: Client,
  private val config: PokemonScraperConfig,
  private val webHook: WebHook
) {
  private val logger = LoggerFactory.getLogger(PokemonScraper::class.java)
  private val count: AtomicInteger = AtomicInteger(1)
  private val page: AtomicInteger = AtomicInteger(0)
  
  fun handleGTLPage(page: CustomGTLPagePacket) {
    if (page.listingType != ListingType.MONSTER) return
    page.entries.forEachIndexed { index, entry ->
      if (entry.pokemon == null) {
        logger.error("Corrupted entry ${page.pageID}.${index} ${entry.itemID} - ${entry.price}")
        return@forEachIndexed
      }
      logger.info(
        "${page.pageID}.${index} - ${entry.price} - ${entry.pokemon!!.id}")
      PokemonCache.addEntry(CacheEntry(entry.price, entry.pokemon!!))
    }
    
    // The pokemon section gets more entries than we can process to we only take the first x entries
    if (this.page.get() < config.maxPages) {
      Thread.sleep(config.delay + (Math.random() * config.jitter).toLong())
      sendRequest()
      return
    }
    
    logger.info("Finished scraping")
    client.shutdown()
    val snapshotID = PokemonCache.persist(Database(config.database))
    if (snapshotID == -1) {
      logger.error("Failed to persist cache")
      webHook.sendMessage(
        webHook
          .createMessage()
          .addEmbed(
            WebHook.WebHookEmbed()
              .setTitle("PokemonScraper Error")
              .setDescription("Failed to persist cache")
              .setAuthor("PokeMMOHub.com", "https://pokemmohub.com")
              .setFooter("by Fiereu", "https://fiereu.de")))
    } else {
      logger.info("Persisted cache with snapshotID $snapshotID")
      webHook.sendMessage(
        webHook
          .createMessage()
          .addEmbed(
            WebHook.WebHookEmbed()
              .setTitle("PokemonScraper finished")
              .setDescription("Stored entries in database")
              .addField("SnapshotID", snapshotID.toString(), true)
              .addField("Version", getPokeMMOVersion().toString(), true)
              .addField("Pages", count.get().toString(), true)
              .setAuthor("PokeMMOHub.com", "https://pokemmohub.com")
              .setFooter("by Fiereu", "https://fiereu.de")))
    }
  }
  fun sendRequest() =
    client.send(
      target = ServerType.GAME,
      packet =
      RequestGTLPagePacket(
        count.getAndIncrement().toByte(),
        ListingType.MONSTER,
        page.getAndIncrement().toShort(),
        emptyArray()))
}

fun getPokeMMOVersion(): Int {
  val url = "https://dl.pokemmo.com/feeds/main_feed.txt"
  val request = Request.Builder().url(url).build()
  val response = OkHttpClient().newCall(request).execute()
  if (!response.isSuccessful) return -1
  val body = response.body?.string() ?: return -1
  val regex = Regex("<min_revision>(\\d+)</min_revision>")
  val match = regex.find(body) ?: return -1
  return match.groupValues[1].toInt()
}

fun main() {
  val logger = LoggerFactory.getLogger("PokemonScraper")
  
  val config =
    ConfigLoaderBuilder.default().build().loadConfigOrThrow<PokemonScraperConfig>("/config.json")

  val webHook = WebHook(config.webhook)
  val version = getPokeMMOVersion()
  
  if (version == -1) {
    logger.error("Failed to get PokeMMO version")
    webHook.sendMessage(
      webHook
        .createMessage()
        .addEmbed(
          WebHook.WebHookEmbed()
            .setTitle("PokemonScraper Error")
            .setDescription("Failed to get PokeMMO version")
            .setAuthor("PokeMMOHub.com", "https://pokemmohub.com")
            .setFooter("by Fiereu", "https://fiereu.de")))
    return
  }
  
  val accountConfig = config.account
  val clientConfig = ClientConfig(
    Account(
      accountConfig.username,
      accountConfig.password,
      accountConfig.hwid.toByteArray(charset = Charsets.UTF_8)
    ),
    onClientReady = {
      Thread.sleep(5000)
      CustomGTLPagePacket.pokemonScraper = PokemonScraper(it.client, config, webHook)
      CustomGTLPagePacket.pokemonScraper!!.sendRequest()
    },
    version = version,
    macAddress = accountConfig.getMacAddress(),
  )
  
  val client = Client(clientConfig)
  GamePacketFactory.registerPacket(0x9B.toByte(), CustomGTLPagePacket::class)
  client.login()
  
  webHook.sendMessage(
    webHook
      .createMessage()
      .addEmbed(
        WebHook.WebHookEmbed()
          .setTitle("PokemonScraper")
          .setDescription("Started scraping PokeMMO GTL - version $version")
          .setAuthor("PokeMMOHub.com", "https://pokemmohub.com")
          .setFooter("by Fiereu", "https://fiereu.de")))
}