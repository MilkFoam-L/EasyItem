package pers.neige.easyitem.command.subcommand

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.easyitem.EasyItem.bukkitScheduler
import pers.neige.easyitem.EasyItem.plugin
import pers.neige.easyitem.command.subcommand.Help.help
import pers.neige.easyitem.manager.ItemManager
import pers.neige.easyitem.utils.LangUtils.sendLang
import pers.neige.neigeitems.utils.PlayerUtils.giveItems
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.module.nms.getName

object Give {
    // ei get [物品ID] (数量) > 根据ID获取NI物品
    val get = subCommand {
        execute<Player> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ei get [物品ID]
        dynamic {
            suggestion<Player>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            execute<Player> { sender, _, argument ->
                giveCommandAsync(sender, sender, argument, "1")
            }
            // ei get [物品ID] (数量)
            dynamic(optional = true) {
                suggestion<Player>(uncheck = true) { _, _ ->
                    arrayListOf("amount")
                }
                execute<Player> { sender, context, argument ->
                    giveCommandAsync(sender, sender, context.argument(-1), argument)
                }
            }
        }
    }

    // ei give [玩家ID] [物品ID] (数量) > 根据ID给予NI物品
    val give = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        dynamic {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            execute<CommandSender> { sender, _, _ ->
                submit(async = true) {
                    help(sender)
                }
            }
            // ei give [玩家ID] [物品ID]
            dynamic {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    ItemManager.items.keys.toList()
                }
                execute<CommandSender> { sender, context, argument ->
                    giveCommandAsync(sender, Bukkit.getPlayerExact(context.argument(-1)), argument, "1")
                }
                // ei give [玩家ID] [物品ID] (数量)
                dynamic(optional = true) {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        arrayListOf("amount")
                    }
                    execute<CommandSender> { sender, context, argument ->
                        giveCommandAsync(sender, Bukkit.getPlayerExact(context.argument(-2)), context.argument(-1), argument)
                    }
                }
            }
        }
    }

    // ei giveAll [物品ID] (数量) > 根据ID给予所有人NI物品
    val giveAll = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ei giveAll [物品ID]
        dynamic {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            execute<CommandSender> { sender, _, argument ->
                giveAllCommandAsync(sender, argument, "1")
            }
            // ei giveAll [物品ID] (数量)
            dynamic(optional = true) {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    arrayListOf("amount")
                }
                execute<CommandSender> { sender, context, argument ->
                    giveAllCommandAsync(sender, context.argument(-1), argument)
                }
            }
        }
    }

    private fun giveCommand(
        // 行为发起人, 用于接收反馈信息
        sender: CommandSender,
        // 物品接收者
        player: Player?,
        // 待给予物品ID
        id: String,
        // 给予数量
        amount: String?
    ) {
        giveCommand(sender, player, id, amount?.toIntOrNull())
    }

    private fun giveCommandAsync(
        sender: CommandSender,
        player: Player?,
        id: String,
        amount: String? = null
    ) {
        submit(async = true) {
            giveCommand(sender, player, id, amount)
        }
    }

    private fun giveAllCommandAsync(
        sender: CommandSender,
        id: String,
        amount: String? = null
    ) {
        submit(async = true) {
            Bukkit.getOnlinePlayers().forEach { player ->
                giveCommand(sender, player, id, amount)
            }
        }
    }

    private fun giveCommand(
        sender: CommandSender,
        player: Player?,
        id: String,
        amount: Int?
    ) {
        player?.let {
            // 获取数量
            amount?.let {
                // 给物品
                ItemManager.getItemStack(id)?.let { itemStack ->
                    bukkitScheduler.callSyncMethod(plugin) {
                        player.giveItems(itemStack, amount.coerceAtLeast(1))
                    }
                    sender.sendLang("Messages.successInfo", mapOf(
                        Pair("{player}", player.name),
                        Pair("{amount}", amount.toString()),
                        Pair("{name}", itemStack.getName())
                    ))
                    player.sendLang("Messages.givenInfo", mapOf(
                        Pair("{amount}", amount.toString()),
                        Pair("{name}", itemStack.getName())
                    ))
                    // 未知物品ID
                } ?: let {
                    sender.sendLang("Messages.unknownItem", mapOf(
                        Pair("{itemID}", id)
                    ))
                }
                // 无效数字
            } ?: let {
                sender.sendLang("Messages.invalidAmount")
            }
            // 无效玩家
        } ?: let {
            sender.sendLang("Messages.invalidPlayer")
        }
    }
}