package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import lu.r3flexi0n.bungeeonlinetime.common.WrappedOnlineTimeCommand
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class OnlineTimeCommand(private val pl: VelocityOnlineTimePlugin) : SimpleCommand {

    private val wrappedCommand = WrappedOnlineTimeCommand(
        pl.logger, pl.config, pl.database,
        { pl.onlineTimePlayers }, { pl.proxy.getPlayer(it).map(WrappedVelocityPlayer::of).orElse(null) }
    )

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        if (sender !is Player) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(pl.config.language.onlyPlayer))
            return
        }
        wrappedCommand.execute(WrappedVelocityPlayer.of(sender), invocation.arguments())
    }


    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val args = invocation.arguments()
        val player = invocation.source() as? Player ?: return emptyList()
        return wrappedCommand.onTabComplete(WrappedVelocityPlayer.of(player), args)
    }
}