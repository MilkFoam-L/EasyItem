package pers.neige.easyitem

import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.platform.BukkitPlugin

object EasyItem : Plugin() {
    val plugin by lazy { BukkitPlugin.getInstance() }

    val bukkitScheduler by lazy { Bukkit.getScheduler() }
}