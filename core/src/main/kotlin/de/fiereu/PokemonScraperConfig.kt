package de.fiereu

data class AccountConfig(
    val username: String,
    val password: String,
    val hwid: String,
    private val mac: String,
) {
    fun getMacAddress(): ByteArray {
        return mac.split(":").map { it.toInt(16).toByte() }.toByteArray()
    }
}

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
)

data class PokemonScraperConfig(
    val account: AccountConfig,
    val database: DatabaseConfig,
    val delay: Long,
    val jitter: Long,
    val maxPages: Int,
    val webhook: String,
)