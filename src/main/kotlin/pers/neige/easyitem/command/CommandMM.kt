package pers.neige.easyitem.command

import pers.neige.easyitem.command.subcommand.MMSave
import taboolib.common.platform.command.CommandBody

/**
 * MM物品兼容指令
 */
object CommandMM {
    @CommandBody
    // ei mm load [物品ID] (保存路径) > 将对应ID的MM物品保存为EI物品
    val load = MMSave.load

    @CommandBody
    // ei mm cover [物品ID] (保存路径) > 将对应ID的MM物品覆盖为EI物品
    val cover = MMSave.cover

    @CommandBody
    // ei mm loadAll (保存路径) > 将全部MM物品转化为EI物品
    val loadAll = MMSave.loadAll
}