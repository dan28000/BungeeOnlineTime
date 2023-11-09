package lu.r3flexi0n.bungeeonlinetime.common.utils

import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.UUID

object Utils {

    fun createPluginMessageArr(otp: OnlineTimePlayer, uuid: UUID): ByteArray? {
        val savedOnlineTime = otp.savedOnlineTime ?: return null
        val totalOnlineTime = savedOnlineTime + otp.getSessionOnlineTime()

        val baos = ByteArrayOutputStream()
        val data = DataOutputStream(baos)
        data.writeUTF(uuid.toString())
        data.writeLong(totalOnlineTime / 1000)
        return baos.toByteArray()
    }

    private const val PM_NAMESPACE = "bungeeonlinetime"
    const val PM_CHANNEL_GET = "$PM_NAMESPACE:get"
}