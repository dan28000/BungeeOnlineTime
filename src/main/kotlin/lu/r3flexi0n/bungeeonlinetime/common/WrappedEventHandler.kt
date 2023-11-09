package lu.r3flexi0n.bungeeonlinetime.common

import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.objects.WrappedPlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Utils
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask
import org.slf4j.Logger
import java.util.UUID

class WrappedEventHandler(
    private val config: Config,
    private val onlineTimePlayers: () -> HashMap<UUID, OnlineTimePlayer>,
    private val database: Database,
    private val logger: Logger
) {

    fun onJoin(player: WrappedPlayer) {
        if (!player.hasPermission("onlinetime.save")) {
            return
        }
        val uuid = player.uniqueId
        val onlineTimePlayer = OnlineTimePlayer()
        onlineTimePlayers()[uuid] = onlineTimePlayer
        if (config.plugin.usePlaceholderApi) {
            asyncTask(
                doTask = { database.getOnlineTime(uuid.toString()) },
                onSuccess = { onlineTimePlayer.setSavedOnlineTime(if (it.isNotEmpty()) it[0].time else 0L) },
                onError = { logger.error("Error while loading online time for player ${player.name}.", it) }
            )
        }
    }

    fun onSwitch(player: WrappedPlayer) {
        val onlineTimePlayer = onlineTimePlayers()[player.uniqueId] ?: return
        if (config.plugin.disabledServers.contains(player.currentServerName)) {
            onlineTimePlayer.joinDisabledServer()
        } else {
            onlineTimePlayer.leaveDisabledServer()
        }
        if (config.plugin.usePlaceholderApi && onlineTimePlayer.savedOnlineTime != null) {
            val arr = Utils.createPluginMessageArr(onlineTimePlayer, player.uniqueId)
            if (arr != null)
                player.sendPluginMessageToCurrentServer(Utils.PM_CHANNEL_GET, arr)
        }
    }

    fun onLeave(player: WrappedPlayer) {
        val uuid = player.uniqueId
        val onlinePlayer = onlineTimePlayers().remove(uuid) ?: return
        val time = onlinePlayer.getSessionOnlineTime()
        if (time >= 5000L) {
            asyncTask(
                doTask = { database.updateOnlineTime(uuid.toString(), player.name, time) },
                onSuccess = {},
                onError = { logger.error("Error while saving online time for player ${player.name}.", it) }
            )
        }
    }
}