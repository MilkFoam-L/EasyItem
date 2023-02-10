package pers.neige.easyitem.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.easyitem.command.subcommand.*
import pers.neige.easyitem.command.subcommand.Help.help
import pers.neige.easyitem.manager.ItemManager
import pers.neige.easyitem.manager.ItemManager.getItemStack
import pers.neige.easyitem.utils.LangUtils.sendLang
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.platform.BukkitAdapter

/**
 * 插件指令
 */
@CommandHeader(name = "EasyItems", aliases = ["ei"])
object Command {
    val bukkitAdapter = BukkitAdapter()

    @CommandBody
    val main = mainCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        incorrectSender { sender, _ ->
            sender.sendLang("Messages.onlyPlayer")
        }
        incorrectCommand { sender, _, _, _ ->
            help(sender)
        }
    }

    @CommandBody
    val test = subCommand {
        dynamic {
            suggestion<Player>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            dynamic(optional = true) {
                execute<Player> { sender, context, argument ->
                    submit(async = true) {
                        val time = System.currentTimeMillis()
                        repeat(argument.toIntOrNull() ?: 1) {
                            getItemStack(context.argument(-1))
                        }
                        sender.sendMessage("耗时: ${System.currentTimeMillis() - time}ms")
                    }
                }
            }
        }
    }

    @CommandBody
    // ei get [物品ID] (数量) > 根据ID获取EI物品
    val get = Give.get

    @CommandBody
    // ei give [玩家ID] [物品ID] (数量) > 根据ID给予EI物品
    val give = Give.give

    @CommandBody
    // ei giveAll [物品ID] (数量) > 根据ID给予所有人EI物品
    val giveAll = Give.giveAll

    @CommandBody
    // ei drop [物品ID] [数量] [世界名] [X坐标] [Y坐标] [Z坐标] > 于指定位置掉落EI物品
    val drop = Drop.drop

    @CommandBody
    // ei save [物品ID] (保存路径) > 将手中物品以对应ID保存至对应路径
    val save = Save.save

    @CommandBody
    // ei cover [物品ID] (保存路径) > 将手中物品以对应ID覆盖至对应路径
    val cover = Save.cover

    @CommandBody
    val mm = CommandMM

    @CommandBody
    // ei list (页码) > 查看所有EI物品
    val list = pers.neige.easyitem.command.subcommand.List.list

    @CommandBody
    val reload = Reload.reload

    @CommandBody
    val help = Help.helpCommand
}