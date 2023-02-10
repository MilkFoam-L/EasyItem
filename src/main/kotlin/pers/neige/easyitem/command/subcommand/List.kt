package pers.neige.easyitem.command.subcommand

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import pers.neige.easyitem.command.Command
import pers.neige.easyitem.manager.ConfigManager
import pers.neige.easyitem.manager.ItemManager
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getName
import taboolib.platform.util.hoverItem
import kotlin.math.ceil

object List {
    val list = subCommand {
        execute<CommandSender> { sender, _, _, ->
            listCommandAsync(sender, 1)
        }
        dynamic {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                (1..ceil(ItemManager.itemAmount.toDouble()/ ConfigManager.config.getDouble("ItemList.ItemAmount")).toInt()).toList().map { it.toString() }
            }
            execute<CommandSender> { sender, _, argument ->
                listCommandAsync(sender, argument.toIntOrNull()?:1)
            }
        }
    }

    private fun listCommandAsync (sender: CommandSender, page: Int) {
        submit (async = true) {
            listCommand(sender, page)
        }
    }

    private fun listCommand (
        // 行为发起人, 用于接收反馈信息
        sender: CommandSender,
        // 页码
        page: Int
    ) {
        val pageAmount = ceil(ItemManager.itemAmount.toDouble()/ ConfigManager.config.getDouble("ItemList.ItemAmount")).toInt()
        val realPage = page.coerceAtMost(pageAmount).coerceAtLeast(1)
        // 发送前缀
        ConfigManager.config.getString("ItemList.Prefix")?.let { sender.sendMessage(it) }
        // 预构建待发送信息
        val listMessage = TellrawJson()
        // 获取当前序号
        val prevItemAmount = ((realPage-1)* ConfigManager.config.getInt("ItemList.ItemAmount"))+1
        // 逐个获取物品
        for (index in (prevItemAmount until prevItemAmount + ConfigManager.config.getInt("ItemList.ItemAmount"))) {
            if (index == ItemManager.itemIds.size + 1) break
            val id = ItemManager.itemIds[index-1]
            // 替换信息内变量
            val listItemMessage = (ConfigManager.config.getString("ItemList.ItemFormat") ?: "")
                .replace("{index}", index.toString())
                .replace("{ID}", id)
            // 构建信息及物品
            if (sender is Player) {
                kotlin.runCatching { ItemManager.getItemStack(id) }.getOrNull()?.let { itemStack ->
                    val listItemMessageList = listItemMessage.split("{name}")
                    val listItemRaw = TellrawJson()
                    for ((i, it) in listItemMessageList.withIndex()) {
                        listItemRaw.append(
                            TellrawJson()
                                .append(it)
                                .runCommand("/ei get $id")
                                .hoverText(ConfigManager.config.getString("Messages.clickGiveMessage")?:"")
                        )
                        if (i+1 != listItemMessageList.size) {
                            // 在1.12.2版本, hoverItem难以应对诸如BRICK(砖块)这种物品, 不得已捕获一下报错
                            kotlin.runCatching {
                                TellrawJson()
                                    .append(itemStack.getName())
                                    .hoverItem(itemStack)
                                    .runCommand("/ei get $id")
                            }.getOrNull()?.let {
                                listItemRaw.append(it)
                            } ?: let {
                                listItemRaw.append(
                                    TellrawJson()
                                        .append(itemStack.getName())
                                        .runCommand("/ei get $id")
                                )
                            }
                        }
                    }
                    listItemRaw.sendTo(Command.bukkitAdapter.adaptCommandSender(sender))
                }
            } else {
                kotlin.runCatching { ItemManager.getItemStack(id) }.getOrNull()?.let { itemStack ->
                    sender.sendMessage(listItemMessage.replace("{name}", itemStack.getName()))
                }
            }
        }
        val prevRaw = TellrawJson()
            .append(ConfigManager.config.getString("ItemList.Prev")?:"")
        if (realPage != 1) {
            prevRaw
                .hoverText((ConfigManager.config.getString("ItemList.Prev")?:"") + ": " + (realPage-1).toString())
                .runCommand("/ei list ${realPage-1}")
        }
        val nextRaw = TellrawJson()
            .append(ConfigManager.config.getString("ItemList.Next")?:"")
        if (realPage != pageAmount) {
            nextRaw.hoverText((ConfigManager.config.getString("ItemList.Next")?:"") + ": " + (realPage+1))
            nextRaw.runCommand("/ei list ${realPage+1}")
        }
        var listSuffixMessage = (ConfigManager.config.getString("ItemList.Suffix")?:"")
            .replace("{current}", realPage.toString())
            .replace("{total}", pageAmount.toString())
        if (sender is Player) {
            listSuffixMessage = listSuffixMessage
                .replace("{prev}", "!@#$%{prev}!@#$%")
                .replace("{next}", "!@#$%{next}!@#$%")
            val listSuffixMessageList = listSuffixMessage.split("!@#$%")
            listSuffixMessageList.forEach { value ->
                when (value) {
                    "{prev}" -> listMessage.append(prevRaw)
                    "{next}" -> listMessage.append(nextRaw)
                    else -> listMessage.append(value)
                }
            }
            // 向玩家发送信息
            listMessage.sendTo(Command.bukkitAdapter.adaptCommandSender(sender))
        } else {
            sender.sendMessage(listSuffixMessage
                .replace("{prev}", ConfigManager.config.getString("ItemList.Prev")?:"")
                .replace("{next}", ConfigManager.config.getString("ItemList.Next")?:""))
        }
    }
}