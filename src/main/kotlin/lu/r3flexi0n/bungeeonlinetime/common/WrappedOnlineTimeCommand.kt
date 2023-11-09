package lu.r3flexi0n.bungeeonlinetime.common

import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.objects.WrappedPlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask
import org.slf4j.Logger
import java.time.Duration
import java.util.UUID

class WrappedOnlineTimeCommand(
    private val logger: Logger,
    private val config: Config,
    private val database: Database,
    private val onlineTimePlayers: () -> Map<UUID, OnlineTimePlayer>,
    private val getPlayer: ((String) -> WrappedPlayer?)
) {

    private fun checkPermission(player: WrappedPlayer, permission: String): Boolean {
        if (!player.hasPermission(permission)) {
            send(player, config.language.noPermission)
            return false
        }
        return true
    }

    fun execute(sender: WrappedPlayer, args: Array<String>) {
        val arg0 = if (args.isNotEmpty()) args[0].lowercase() else ""
        val size = args.size

        if (size == 0) {
            if (checkPermission(sender, "onlinetime.own")) {
                sendOnlineTime(sender.name, sender)
            }

        } else if (size == 2 && arg0 == "get") {

            if (checkPermission(sender, "onlinetime.others")) {
                val name = args[1]
                sendOnlineTime(name, sender)
            }

        } else if ((size == 1 || size == 2) && arg0 == "top") {

            if (checkPermission(sender, "onlinetime.top")) {
                val page = (args[1].toIntOrNull() ?: 1).coerceAtLeast(1)
                sendTopOnlineTimes(page, sender)
            }

        } else if (args.size == 2 && arg0 == "reset") {

            if (checkPermission(sender, "onlinetime.reset")) {
                val name = args[1]
                sendReset(name, sender)
                val player = getPlayer(name)
                if (player != null)
                    onlineTimePlayers()[player.uniqueId]?.setSavedOnlineTime(0L)
            }

        } else if (args.size == 1 && arg0 == "resetall") {

            if (checkPermission(sender, "onlinetime.resetall"))
                sendResetAll(sender)

        } else {
            send(sender, config.language.help)
        }
    }

    private fun sendOnlineTime(targetPlayerName: String, sender: WrappedPlayer) {
        asyncTask(doTask = {
            database.getOnlineTime(targetPlayerName)

        }, onSuccess = { response ->
            if (response.isEmpty()) {
                val placeholders = mapOf("%PLAYER%" to targetPlayerName)
                send(sender, config.language.playerNotFound, placeholders)
            } else {
                for (onlineTime in response) {
                    val sessionTime = onlineTimePlayers()[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                    val total = Duration.ofMillis(onlineTime.time + sessionTime)

                    val placeholders = mapOf(//@formatter:off
                        "%PLAYER%"  to onlineTime.name,
                        "%HOURS%"   to total.toHours() % 24,
                        "%MINUTES%" to total.toMinutes() % 60
                    )//@formatter:on
                    send(sender, config.language.onlineTime, placeholders)
                }
            }
        }, onError = { e ->
            send(sender, config.language.error, emptyMap())
            logger.error("Error while loading online time for player $targetPlayerName.", e)
        })
    }

    private fun sendTopOnlineTimes(page: Int, sender: WrappedPlayer) {
        val topOnlineTimePageLimit = config.plugin.topOnlineTimePageLimit
        asyncTask(doTask = {
            database.getTopOnlineTimes(page, topOnlineTimePageLimit)

        }, onSuccess = { response ->
            var rank = (page - 1) * topOnlineTimePageLimit + 1
            val headerPlaceholders = mapOf("%PAGE%" to page)
            send(sender, config.language.topTimeAbove, headerPlaceholders)

            for (onlineTime in response) {
                val sessionTime = onlineTimePlayers()[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(//@formatter:off
                    "%RANK%"    to rank,
                    "%PLAYER%"  to onlineTime.name,
                    "%HOURS%"   to total.toHours() % 24,
                    "%MINUTES%" to total.toMinutes() % 60
                )//@formatter:on
                send(sender, config.language.topTime, placeholders)
                rank++
            }
            send(sender, config.language.topTimeBelow, headerPlaceholders)
        }, onError = { e ->
            send(sender, config.language.error)
            logger.error("Error while loading top online times.", e)
        })
    }

    private fun sendReset(targetPlayerName: String, sender: WrappedPlayer) {
        asyncTask(doTask = {
            database.resetOnlineTime(targetPlayerName)
        }, onSuccess = {
            send(sender, config.language.resetPlayer, mapOf("%PLAYER%" to targetPlayerName))
        }, onError = { e ->
            send(sender, config.language.error, emptyMap())
            logger.error("Error while resetting online time for player $targetPlayerName.", e)
        })
    }

    private fun sendResetAll(sender: WrappedPlayer) {
        asyncTask(doTask = {
            database.resetAllOnlineTimes()
            onlineTimePlayers().forEach { (_, value) -> value.setSavedOnlineTime(0L) }
        }, onSuccess = {
            send(sender, config.language.resetAll, emptyMap())
        }, onError = { e ->
            send(sender, config.language.error, emptyMap())
            logger.error("Error while resetting online time database.", e)
        })
    }

    private fun send(sender: WrappedPlayer, messageId: String, placeholders: Map<String, Any>? = null) {
        var message = messageId
        if (placeholders != null) {
            for ((key, value) in placeholders) {
                message = message.replace(key, value.toString())
            }
        }
        sender.sendMessage(message)
    }

    private val subCommands = mapOf(// @formatter:off
        "get"      to "onlinetime.others",
        "top"      to "onlinetime.top",
        "reset"    to "onlinetime.reset",
        "resetall" to "onlinetime.resetall",
    )// @formatter:on

    fun onTabComplete(player: WrappedPlayer, args: Array<String>): List<String> =
        when (args.size) {
            0, 1 -> subCommands.entries
                .mapNotNull { (k, v) -> k.takeUnless { k.startsWith(args[0], true) && player.hasPermission(v) } }

            2 -> if (args[1] == "top") (3..20).map(Int::toString) else emptyList()
            else -> emptyList()
        }
}