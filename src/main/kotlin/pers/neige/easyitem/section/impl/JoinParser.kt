package pers.neige.easyitem.section.impl

import org.bukkit.configuration.ConfigurationSection
import pers.neige.easyitem.section.SectionParser
import pers.neige.neigeitems.utils.SectionUtils.parseSection

/**
 * join节点解析器
 */
object JoinParser : SectionParser() {
    override val id: String = "join"

    override fun onRequest(
        data: ConfigurationSection,
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?
    ): String? {
        return handler(
            cache,
            sections,
            data.getStringList("list"),
            data.getString("separator"),
            data.getString("prefix"),
            data.getString("postfix"),
            data.getString("limit"),
            data.getString("truncated")
        )
    }

    /**
     * @param cache 解析值缓存
     * @param sections 节点池
     * @param list 待操作列表
     * @param rawSeparator 分隔符
     * @param rawPrefix 前缀
     * @param rawPostfix 后缀
     * @param rawLimit 长度限制
     * @param rawTruncated 删节符号
     * @return 解析值
     */
    private fun handler(
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?,
        list: List<String>?,
        rawSeparator: String?,
        rawPrefix: String?,
        rawPostfix: String?,
        rawLimit: String?,
        rawTruncated: String?
    ): String? {
        // 如果待操作列表存在, 进行后续操作
        list?.let {
            // 获取分隔符(默认为", ")
            val separator = rawSeparator?.parseSection(cache, sections) ?: ", "
            // 获取前缀
            val prefix = rawPrefix?.parseSection(cache, sections) ?: ""
            // 获取后缀
            val postfix = rawPostfix?.parseSection(cache, sections) ?: ""
            // 获取长度限制
            val limit = rawLimit?.parseSection(cache, sections)?.toIntOrNull()?.let {
                when {
                    it >= list.size -> null
                    it < 0 -> 0
                    else -> it
                }
            }
            // 获取删节符号
            val truncated = rawTruncated?.parseSection(cache, sections)

            // 开始构建结果
            val result = StringBuilder()
            // 添加前缀
            result.append(prefix)
            // 获取遍历范围
            val length = limit ?: list.size

            // 遍历列表
            for (index in 0 until length) {
                // 解析元素节点
                val element = list[index].parseSection(cache, sections)
                // 添加元素
                result.append(element)
                // 添加分隔符
                if (index != (length - 1) || (limit != null && truncated != null)) {
                    result.append(separator)
                }
            }
            // 添加删节符号
            if (limit != null && truncated != null) {
                result.append(truncated)
            }
            // 添加后缀
            result.append(postfix)
            // 返回结果
            return result.toString()
        }
        return null
    }
}