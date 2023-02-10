package pers.neige.easyitem.command.subcommand

import org.bukkit.entity.Player
import pers.neige.easyitem.command.subcommand.Help.help
import pers.neige.easyitem.manager.ItemManager
import pers.neige.easyitem.utils.LangUtils.sendLang
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.module.nms.getName
import java.io.File

object Save {
    // ei save [物品ID] (保存路径) > 将手中物品以对应ID保存至对应路径
    val save = subCommand {
        execute<Player> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ei save [物品ID]
        dynamic {
            suggestion<Player>(uncheck = true) { _, _ ->
                arrayListOf("id")
            }
            execute<Player> { sender, _, argument ->
                submit(async = true) {
                    when (ItemManager.saveItem(sender.inventory.itemInMainHand, argument, "$argument.yml", false)) {
                        // 保存成功
                        1 -> {
                            sender.sendLang("Messages.successSaveInfo", mapOf(
                                Pair("{name}", sender.inventory.itemInMainHand.getName()),
                                Pair("{itemID}", argument),
                                Pair("{path}", "$argument.yml")
                            ))
                        }
                        // 已存在对应ID物品
                        0 -> {
                            sender.sendLang("Messages.existedKey", mapOf(
                                Pair("{itemID}", argument)
                            ))
                        }
                        // 你保存了个空气
                        else -> sender.sendLang("Messages.airItem")
                    }
                }
            }
            // ei save [物品ID] (保存路径)
            dynamic {
                suggestion<Player>(uncheck = true) { _, _ ->
                    ItemManager.files.map { it.path.replace("plugins${File.separator}EasyItem${File.separator}Items${File.separator}", "") }
                }
                execute<Player> { sender, context, argument ->
                    submit(async = true) {
                        when (ItemManager.saveItem(sender.inventory.itemInMainHand, context.argument(-1), argument, false)) {
                            // 保存成功
                            1 -> {
                                sender.sendLang("Messages.successSaveInfo", mapOf(
                                    Pair("{name}", sender.inventory.itemInMainHand.getName()),
                                    Pair("{itemID}", context.argument(-1)),
                                    Pair("{path}", argument)
                                ))
                            }
                            // 已存在对应ID物品
                            0 -> {sender.sendLang("Messages.existedKey", mapOf(
                                Pair("{itemID}", context.argument(-1))
                            ))
                            }
                            // 你保存了个空气
                            else -> sender.sendLang("Messages.airItem")
                        }
                    }
                }
            }
        }
    }

    // ei cover [物品ID] (保存路径) > 将手中物品以对应ID覆盖至对应路径
    val cover = subCommand {
        execute<Player> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ei cover [物品ID]
        dynamic {
            suggestion<Player>(uncheck = true) { _, _ ->
                arrayListOf("id")
            }
            execute<Player> { sender, _, argument ->
                submit(async = true) {
                    when (ItemManager.saveItem(sender.inventory.itemInMainHand, argument, "$argument.yml", true)) {
                        // 你保存了个空气
                        2 -> sender.sendLang("Messages.airItem")
                        // 保存成功
                        else -> {
                            sender.sendLang("Messages.successSaveInfo", mapOf(
                                Pair("{name}", sender.inventory.itemInMainHand.getName()),
                                Pair("{itemID}", argument),
                                Pair("{path}", "$argument.yml")
                            ))
                        }
                    }
                }
            }
            // ei cover [物品ID] (保存路径)
            dynamic {
                suggestion<Player>(uncheck = true) { _, _ ->
                    ItemManager.files.map { it.path.replace("plugins${File.separator}EasyItem${File.separator}Items${File.separator}", "") }
                }
                execute<Player> { sender, context, argument ->
                    submit(async = true) {
                        when (ItemManager.saveItem(sender.inventory.itemInMainHand, context.argument(-1), argument, true)) {
                            // 你保存了个空气
                            2 -> sender.sendLang("Messages.airItem")
                            // 保存成功
                            else -> {
                                sender.sendLang("Messages.successSaveInfo", mapOf(
                                    Pair("{name}", sender.inventory.itemInMainHand.getName()),
                                    Pair("{itemID}", context.argument(-1)),
                                    Pair("{path}", argument)
                                ))
                            }
                        }
                    }
                }
            }
        }
    }
}