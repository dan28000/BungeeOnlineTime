package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import lu.r3flexi0n.bungeeonlinetime.common.WrappedEventHandler

class OnlineTimeListener(plugin: VelocityOnlineTimePlugin) {
    private val wrappedEventHandler =
        WrappedEventHandler(plugin.config, { plugin.onlineTimePlayers }, plugin.database, plugin.logger)


    @Subscribe
    fun onJoin(e: PostLoginEvent) =
        wrappedEventHandler.onJoin(WrappedVelocityPlayer.of(e.player))

    @Subscribe
    @Suppress("UnstableApiUsage")
    fun onSwitch(e: ServerPostConnectEvent) =
        wrappedEventHandler.onSwitch(WrappedVelocityPlayer.of(e.player))

    @Subscribe
    fun onLeave(e: DisconnectEvent) =
        wrappedEventHandler.onLeave(WrappedVelocityPlayer.of(e.player))
}