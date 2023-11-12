package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import lu.r3flexi0n.bungeeonlinetime.common.objects.WrappedPlayer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.UUID

class WrappedVelocityPlayer(private val player: Player) : WrappedPlayer() {
    override val name: String get() = player.username

    override val uniqueId: UUID get() = player.uniqueId

    override fun sendMessage(msg: String) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg))
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override val currentServerName: String
        get() = player.currentServer.get().serverInfo.name

    override fun sendPluginMessageToCurrentServer(channel: String, data: ByteArray) {
        player.currentServer.get().sendPluginMessage(MinecraftChannelIdentifier.from(channel), data)
    }

    override fun unload() {
        cache.remove(player)
    }


    companion object {
        private val cache = HashMap<Player, WrappedPlayer>()

        fun of(player: Player): WrappedPlayer {
            return cache.getOrPut(player) { WrappedVelocityPlayer(player) }
        }
    }
}