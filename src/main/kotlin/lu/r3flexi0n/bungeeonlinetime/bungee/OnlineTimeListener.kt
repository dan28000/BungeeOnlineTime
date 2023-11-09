package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.WrappedEventHandler
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class OnlineTimeListener(plugin: BungeeOnlineTimePlugin) : Listener {
    private val wrappedEventHandler =
        WrappedEventHandler(plugin.config, { plugin.onlineTimePlayers }, plugin.database, plugin.logger)


    @EventHandler
    fun onJoin(e: PostLoginEvent) =
        wrappedEventHandler.onJoin(WrappedBungeePlayer.of(e.player))

    @EventHandler
    fun onSwitch(e: ServerSwitchEvent) =
        wrappedEventHandler.onSwitch(WrappedBungeePlayer.of(e.player))

    @EventHandler
    fun onLeave(e: PlayerDisconnectEvent) =
        wrappedEventHandler.onLeave(WrappedBungeePlayer.of(e.player))
}