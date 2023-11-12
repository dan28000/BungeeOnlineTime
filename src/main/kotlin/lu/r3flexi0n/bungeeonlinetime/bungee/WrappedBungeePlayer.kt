package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.objects.WrappedPlayer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.UUID

class WrappedBungeePlayer(private val player: ProxiedPlayer) : WrappedPlayer() {
    override val name: String get() = player.name

    override val uniqueId: UUID get() = player.uniqueId

    override fun sendMessage(msg: String) {
        player.sendMessage(TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', msg)))
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override val currentServerName: String
        get() = player.server.info.name

    override fun sendPluginMessageToCurrentServer(channel: String, data: ByteArray) {
        player.server.sendData(channel, data)
    }

    override fun unload() {
        cache.remove(player)
    }

    companion object {
        private val cache = HashMap<ProxiedPlayer, WrappedPlayer>()

        fun of(player: ProxiedPlayer): WrappedPlayer {
            return cache.getOrPut(player) { WrappedBungeePlayer(player) }
        }
    }
}