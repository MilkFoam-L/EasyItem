package pers.neige.easyitem.command.subcommand

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import pers.neige.easyitem.command.subcommand.Help.help
import pers.neige.easyitem.manager.ItemManager
import pers.neige.easyitem.utils.LangUtils.sendLang
import pers.neige.neigeitems.utils.ItemUtils.dropNiItems
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.module.nms.getName

object Drop {
    val drop = subCommand {
        // ei drop [物品ID]
        dynamic {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            execute<CommandSender> { sender, _, _ ->
                submit(async = true) {
                    help(sender)
                }
            }
            // ei drop [物品ID] [数量]
            dynamic {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    arrayListOf("amount")
                }
                execute<CommandSender> { sender, _, _ ->
                    submit(async = true) {
                        help(sender)
                    }
                }
                // ei drop [物品ID] [数量] [世界名]
                dynamic {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        Bukkit.getWorlds().map { it.name }
                    }
                    execute<CommandSender> { sender, _, _ ->
                        submit(async = true) {
                            help(sender)
                        }
                    }
                    // ei drop [物品ID] [数量] [世界名] [X坐标]
                    dynamic {
                        suggestion<CommandSender>(uncheck = true) { _, _ ->
                            arrayListOf("x")
                        }
                        execute<CommandSender> { sender, _, _ ->
                            submit(async = true) {
                                help(sender)
                            }
                        }
                        // ei drop [物品ID] [数量] [世界名] [X坐标] [Y坐标]
                        dynamic {
                            suggestion<CommandSender>(uncheck = true) { _, _ ->
                                arrayListOf("y")
                            }
                            execute<CommandSender> { sender, _, _ ->
                                submit(async = true) {
                                    help(sender)
                                }
                            }
                            // ei drop [物品ID] [数量] [世界名] [X坐标] [Y坐标] [Z坐标]
                            dynamic {
                                suggestion<CommandSender>(uncheck = true) { _, _ ->
                                    arrayListOf("z")
                                }
                                execute<CommandSender> { sender, context, argument ->
                                    dropCommandAsync(
                                        sender,
                                        context.argument(-5),
                                        context.argument(-4),
                                        context.argument(-3),
                                        context.argument(-2),
                                        context.argument(-1),
                                        argument
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun dropCommand(
        // 行为发起人, 用于接收反馈信息
        sender: CommandSender,
        // 待掉落物品ID
        id: String,
        // 掉落数量
        amount: String,
        // 掉落世界名
        worldName: String,
        // 掉落世界x坐标
        xString: String,
        // 掉落世界y坐标
        yString: String,
        // 掉落世界z坐标
        zString: String
    ) {
        Bukkit.getWorld(worldName)?.let { world ->
            val x = xString.toDoubleOrNull()
            val y = yString.toDoubleOrNull()
            val z = zString.toDoubleOrNull()
            if (x != null && y != null && z != null) {
                dropCommand(sender, id, amount.toIntOrNull(), Location(world, x, y, z))
            } else {
                sender.sendLang("Messages.invalidLocation")
            }
        } ?: let {
            sender.sendLang("Messages.invalidWorld")
        }
    }

    private fun dropCommandAsync(
        sender: CommandSender,
        id: String,
        amount: String,
        worldName: String,
        xString: String,
        yString: String,
        zString: String
    ) {
        submit(async = true) {
            dropCommand(sender, id, amount, worldName, xString, yString, zString)
        }
    }

    private fun dropCommand(
        sender: CommandSender,
        id: String,
        amount: Int?,
        location: Location?
    ) {
        // 获取数量
        amount?.let {
            // 掉物品
            ItemManager.getItemStack(id)?.also { itemStack ->
                location?.dropNiItems(itemStack, amount.coerceAtLeast(1))
                sender.sendLang("Messages.dropSuccessInfo", mapOf(
                    Pair("{world}", location?.world?.name ?: ""),
                    Pair("{x}", location?.x.toString()),
                    Pair("{y}", location?.y.toString()),
                    Pair("{z}", location?.z.toString()),
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
    }
}