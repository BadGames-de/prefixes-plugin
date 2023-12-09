package app.simplecloud.plugin.prefixes.shared

import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.PrefixesNameElement
import net.kyori.adventure.text.TextComponent
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import java.util.*
import java.util.concurrent.CompletableFuture

class LuckPermsGroup(private var group: Group, private var luckPerms: LuckPerms) : PrefixesGroup<TextComponent> {

    override fun getName(): String {
        return group.name;
    }

    override fun getPrefix(): PrefixesNameElement<TextComponent> {
        return PrefixesComponentNameElement((group.cachedData.metaData.prefix ?: "").replace("&", "§"))
    }

    override fun getColor(): PrefixesNameElement<TextComponent> {
        return PrefixesComponentNameElement(group.cachedData.metaData.getMetaValue("color") ?: "")
    }

    override fun getSuffix(): PrefixesNameElement<TextComponent> {
        return PrefixesComponentNameElement((group.cachedData.metaData.suffix ?: "").replace("&", "§"))
    }

    override fun getPriority(): Int {
        return group.weight.orElse(0)
    }

    override fun containsPlayer(uniqueId: UUID): Boolean {
        return containsPlayerFuture(uniqueId).join()
    }

    override fun containsPlayerFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return luckPerms.userManager.loadUser(uniqueId).thenApplyAsync { user ->
            return@thenApplyAsync user.primaryGroup == getName()
        }
    }
}