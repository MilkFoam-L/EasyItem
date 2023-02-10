package pers.neige.easyitem.section.impl

import org.bukkit.configuration.ConfigurationSection
import pers.neige.easyitem.asahi.util.calculate.calculate
import pers.neige.easyitem.section.SectionParser
import pers.neige.easyitem.utils.SectionUtils.parseSection

/**
 * 公式节点解析器
 */
object FastCalcParser : SectionParser() {
    override val id: String = "fastcalc"

    override fun onRequest(
        data: ConfigurationSection,
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?
    ): String? {
        return handler(
            cache,
            sections,
            true,
            data.getString("formula"),
            data.getString("fixed"),
            data.getString("min"),
            data.getString("max")
        )
    }

    override fun onRequest(
        args: List<String>,
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?
    ): String {
        return handler(
            cache,
            sections,
            false,
            args.getOrNull(0),
            args.getOrNull(1),
            args.getOrNull(2),
            args.getOrNull(3)
        ) ?: "<$id::${args.joinToString("_")}>"
    }

    /**
     * @param cache 解析值缓存
     * @param player 待解析玩家
     * @param sections 节点池
     * @param parse 是否对参数进行节点解析
     * @param fomulaString 公式文本
     * @param fixedString 取整位数文本
     * @param minString 最小值文本
     * @param maxString 最大值文本
     * @return 解析值
     */
    private fun handler(
        cache: HashMap<String, String>?,
        sections: ConfigurationSection?,
        parse: Boolean,
        fomulaString: String?,
        fixedString: String?,
        minString: String?,
        maxString: String?
    ): String? {
        try {
            // 加载公式
            fomulaString?.parseSection(parse, cache, sections)?.let {
                // 计算结果
                var result = it.calculate()
                // 获取大小范围
                minString?.parseSection(parse, cache, sections)?.toDouble()?.let { min ->
                    result = min.coerceAtLeast(result)
                }
                maxString?.parseSection(parse, cache, sections)?.toDouble()?.let { max ->
                    result = max.coerceAtMost(result)
                }
                // 获取取整位数
                val fixed = fixedString?.parseSection(parse, cache, sections)?.toIntOrNull() ?: 0
                // 加载结果
                return "%.${fixed}f".format(result)
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
        return null
    }
}