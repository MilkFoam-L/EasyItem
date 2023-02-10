package pers.neige.easyitem.manager

import pers.neige.easyitem.section.SectionParser
import pers.neige.easyitem.section.impl.FastCalcParser
import pers.neige.easyitem.section.impl.GradientParser
import pers.neige.easyitem.section.impl.JoinParser
import pers.neige.easyitem.section.impl.RepeatParser
import java.util.concurrent.ConcurrentHashMap

/**
 * 全局节点 & 节点解析器管理器
 *
 * @constructor 构建全局节点 & 节点解析器管理器
 */
object SectionManager {
    /**
     * 获取所有节点解析器
     */
    val sectionParsers = ConcurrentHashMap<String, SectionParser>()

    init {
        // 加载基础节点解析器
        loadBasicParser()
    }

    /**
     * 用于加载节点解析器
     *
     * @param sectionParser 节点解析器
     */
    fun loadParser(sectionParser: SectionParser) {
        sectionParsers[sectionParser.id] = sectionParser
    }

    /**
     * 加载基础节点解析器
     */
    private fun loadBasicParser() {
        FastCalcParser.register()
        GradientParser.register()
        JoinParser.register()
        RepeatParser.register()
    }
}