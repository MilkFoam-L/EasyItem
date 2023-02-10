package pers.neige.easyitem.utils

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import pers.neige.easyitem.manager.SectionManager
import pers.neige.easyitem.section.Section
import pers.neige.neigeitems.utils.SectionUtils.parse
import pers.neige.neigeitems.utils.StringUtils.split
import java.awt.Color

/**
 * 节点相关工具类
 */
object SectionUtils {
    /**
     * 对文本进行节点解析
     *
     * @param cache 解析值缓存
     * @param sections 节点池
     * @return 解析值
     */
    @JvmStatic
    fun String.parseSection(
        cache: HashMap<String, String>? = null,
        sections: ConfigurationSection? = null
    ): String {
        return this.parse {
            return@parse it.getSection(cache, sections)
        }
    }

    /**
     * 对文本进行节点解析
     *
     * @return 解析值
     */
    @JvmStatic
    fun String.parseSection(): String {
        return this.parseSection(null, null)
    }

    /**
     * 对文本进行节点解析
     * parse参数为false时不对文本进行解析
     * 用意为不解析即时声明节点的参数(处理即时节点的SectionParser.onRequest时将parse定义为false, 解析参数时传入parse即可)
     * 因为即时声明节点能传入进来一定是经过了parseSection
     * 而这一步会对文本进行全局节点解析
     * 即: 参数为解析后分割传入的
     * 对于已解析的参数, 多解析一次等于浪费时间
     *
     * @param parse 是否对文本进行节点解析
     * @param cache 解析值缓存
     * @param sections 节点池
     * @return 解析值
     */
    @JvmStatic
    fun String.parseSection(
        parse: Boolean,
        cache: HashMap<String, String>? = null,
        sections: ConfigurationSection? = null
    ): String {
        return when {
            parse -> this.parseSection(cache, sections)
            else -> this
        }
    }

    /**
     * 对节点内容进行解析 (已经去掉 <>)
     *
     * @param cache 解析值缓存
     * @param sections 节点池
     * @return 解析值
     */
    @JvmStatic
    fun String.getSection(
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?
    ): String {
        when (val index = this.indexOf("::")) {
            // 私有节点调用
            -1 -> {
                // 尝试读取缓存
                if (cache?.get(this) != null) {
                    // 直接返回对应节点值
                    return cache[this] as String
                    // 读取失败, 尝试主动解析
                } else {
                    // 尝试解析并返回对应节点值
                    if (sections != null && sections.contains(this)) {
                        // 获取节点ConfigurationSection
                        val section = sections.getConfigurationSection(this)
                        // 简单节点
                        if (section == null) {
                            val result = sections.getString(this)?.parseSection(cache, sections) ?: "<$this>"
                            cache?.put(this, result)
                            return result
                        }
                        // 加载节点
                        return Section(section, this).load(cache, sections) ?: "<$this>"
                    }
                    if (this.startsWith("#")) {
                        try {
                            try {
                                val hex = (this.substring(1).toIntOrNull(16) ?: 0)
                                    .coerceAtLeast(0)
                                    .coerceAtMost(0xFFFFFF)
                                val color = Color(hex)
                                return ChatColor.of(color).toString()
                            } catch (_: NumberFormatException) {}
                        } catch (error: NoSuchMethodError) {
                            Bukkit.getLogger().info("§e[EI] §6低于1.16的版本不能使用16进制颜色哦")
                        }
                    }
                }
                return "<$this>"
            }
            // 即时声明节点解析
            else -> {
                // 获取节点类型
                val type = this.substring(0, index)
                // 所有参数
                val args = this.substring(index+2).split('_', '\\')
                return SectionManager.sectionParsers[type]?.onRequest(args, cache, sections) ?: "<$this>"
            }
        }
    }
}