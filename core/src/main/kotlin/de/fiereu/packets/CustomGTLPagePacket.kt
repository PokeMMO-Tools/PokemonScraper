package de.fiereu.packets

import de.fiereu.PokemonScraper
import de.fiereu.pokemmo.headless.network.server.AbstractServer
import de.fiereu.pokemmo.headless.network.packets.game.deserializable.GTLPagePacket

class CustomGTLPagePacket(id: Byte): GTLPagePacket(id) {

    companion object {
        var pokemonScraper: PokemonScraper? = null
    }

    override fun handle(server: AbstractServer) {
        if(pokemonScraper == null) return super.handle(server)
        pokemonScraper!!.handleGTLPage(this)
    }
}