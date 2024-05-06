package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.impl.PrefixesConfigImpl
import app.simplecloud.plugin.prefixes.shared.PrefixesApiLuckPermsImpl
import app.simplecloud.plugin.prefixes.shared.PrefixesConfigParser
import app.simplecloud.plugin.prefixes.spigot.PrefixesActorSpigotImpl
import app.simplecloud.plugin.prefixes.spigot.PrefixesGlobalDisplaySpigotImpl
import com.comphenix.protocol.ProtocolLibrary
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.identity.Identity
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class PrefixesPlugin : JavaPlugin(), Listener {

    private lateinit var prefixesApi: PrefixesApiLuckPermsImpl
    private val scoreboard: PrefixesGlobalDisplaySpigotImpl = PrefixesGlobalDisplaySpigotImpl()

    override fun onEnable() {
        val manager = ProtocolLibrary.getProtocolManager()
        val prefixesApiActor = PrefixesActorSpigotImpl(manager, scoreboard)
        val luckPermsProvider: RegisteredServiceProvider<LuckPerms> =
            Bukkit.getServicesManager().getRegistration(LuckPerms::class.java) ?: return
        val luckPerms: LuckPerms = luckPermsProvider.provider
        prefixesApi = PrefixesApiLuckPermsImpl(luckPerms)
        prefixesApi.setActor(prefixesApiActor)
        prefixesApi.setConfig(
            PrefixesConfigParser<PrefixesConfigImpl>(File(dataFolder, "config.json")).parse(
                PrefixesConfigImpl::class.java,
                PrefixesConfigImpl()
            )
        )
        saveResource("config.json", false)
        prefixesApi.indexGroups()
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getServicesManager().register(PrefixesApi::class.java, prefixesApi, this, ServicePriority.Normal)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val uniqueId: UUID = event.player.uniqueId
        val playerGroup: PrefixesGroup = prefixesApi.getHighestGroup(uniqueId)
        prefixesApi.setWholeName(uniqueId, playerGroup)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncChatEvent) {
        event.renderer { player, _, message, audience ->
            val viewer = audience.getOrDefault(Identity.UUID, null) ?: return@renderer message
            prefixesApi.formatChatMessage(player.uniqueId, viewer, prefixesApi.getConfig().getChatFormat(), message)
        }
    }


}