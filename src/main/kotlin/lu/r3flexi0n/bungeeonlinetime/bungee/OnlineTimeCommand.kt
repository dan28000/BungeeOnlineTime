package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.WrappedOnlineTimeCommand
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor

class OnlineTimeCommand(private val pl: BungeeOnlineTimePlugin, cmd: String, aliases: Array<String>) :
    Command(cmd, null, *aliases), TabExecutor {

    private val wrappedCommand = WrappedOnlineTimeCommand(
        pl.logger, pl.config, pl.database,
        { pl.onlineTimePlayers }, { pl.proxy.getPlayer(it)?.let(WrappedBungeePlayer::of) }
    )

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is ProxiedPlayer) {
            pl.logger.info(ChatColor.translateAlternateColorCodes('&', pl.config.language.onlyPlayer))
            return
        }
        wrappedCommand.execute(WrappedBungeePlayer.of(sender), args)
    }


    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> =
        if (sender is ProxiedPlayer) {
            wrappedCommand.onTabComplete(WrappedBungeePlayer.of(sender), args)
        } else
            emptyList()
}