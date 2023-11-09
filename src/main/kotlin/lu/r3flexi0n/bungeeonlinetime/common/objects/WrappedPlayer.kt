package lu.r3flexi0n.bungeeonlinetime.common.objects

import java.util.UUID

abstract class WrappedPlayer {
    abstract val name: String

    abstract val uniqueId: UUID

    abstract fun sendMessage(msg: String)

    abstract fun hasPermission(permission: String): Boolean

    abstract val currentServerName: String

    abstract fun sendPluginMessageToCurrentServer(channel: String, data: ByteArray)
}