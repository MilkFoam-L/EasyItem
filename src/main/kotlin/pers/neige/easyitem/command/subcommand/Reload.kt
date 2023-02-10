package pers.neige.easyitem.command.subcommand

import org.bukkit.command.CommandSender
import pers.neige.easyitem.manager.ConfigManager
import pers.neige.easyitem.manager.ItemManager
import pers.neige.easyitem.utils.LangUtils.sendLang
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit

object Reload {
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            reloadCommand(sender)
        }
    }

    private fun reloadCommand(sender: CommandSender) {
        submit(async = true) {
            ConfigManager.reload()
            ItemManager.reload()
            sender.sendLang("Messages.reloadedMessage")
        }
    }
}