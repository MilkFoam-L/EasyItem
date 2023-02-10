package pers.neige.easyitem.section.impl

import org.bukkit.configuration.ConfigurationSection
import pers.neige.easyitem.section.SectionParser
import pers.neige.neigeitems.utils.SectionUtils.parseSection

/**
 * repeat节点解析器
 */
object RepeatParser : SectionParser() {
    override val id: String = "repeat"

    override fun onRequest(
        data: ConfigurationSection,
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?
    ): String {
        return handler(
            cache,
            sections,
            data.getString("content")?.parseSection(cache, sections) ?: "",
            data.getString("separator")?.parseSection(cache, sections),
            data.getString("prefix")?.parseSection(cache, sections),
            data.getString("postfix")?.parseSection(cache, sections),
            data.getString("repeat")?.parseSection(cache, sections)?.toIntOrNull()
        )
    }

    /**
     * @param cache 解析值缓存
     * @param sections 节点池
     * @param content 待重复内容
     * @param separator 分隔符
     * @param prefix 前缀
     * @param postfix 后缀
     * @param repeat 长度限制
     * @param transform 操作函数
     * @return 解析值
     */
    private fun handler(
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?,
        content: String,
        separator: String?,
        prefix: String?,
        postfix: String?,
        repeat: Int?
    ): String {
        // 获取长度限制
        val length = (repeat ?: 1).coerceAtLeast(0)

        // 开始构建结果
        val result = StringBuilder()
        // 添加前缀
        prefix?.let {
            result.append(it)
        }

        for (index in 0 until length) {
            // 添加元素
            result.append(content)
            // 添加分隔符
            separator?.let {
                if (index != (length - 1)) {
                    result.append(it)
                }
            }
        }
        // 添加后缀
        postfix?.let {
            result.append(it)
        }
        // 返回结果
        return result.toString()
    }
}