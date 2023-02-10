package pers.neige.easyitem.section

import org.bukkit.configuration.ConfigurationSection
import pers.neige.easyitem.manager.SectionManager
import taboolib.common.platform.function.info

/**
 * 用于对节点的ConfigurationSection进行包装, 方便地获取或缓存解析值
 * configSection.name只能获得末级ID, 难以解决形似a.b.c的多级调用
 * 因此需要特别指定ID
 *
 * @property configSection 节点配置
 * @property id 节点ID
 * @constructor 编译js脚本并进行包装
 */
class Section(configSection: ConfigurationSection, val id: String = configSection.name) {
    /**
     * 获取节点类型
     */
    val type = configSection.getString("type")

    /**
     * 获取节点内容
     */
    val data: ConfigurationSection = configSection

    /**
     * 获取节点解析值
     *
     * @param cache 解析值缓存
     * @param sections 节点池
     * @return 解析值
     */
    fun get(
        cache: HashMap<String, String>? = null,
        sections: ConfigurationSection? = null
    ): String? {
        // 空类型(可能是多层节点)
        type?.let {
            // 空解析器
            SectionManager.sectionParsers[type]?.let { parser ->
                // 空返回值(只有解析出错才允许返回空值)
                parser.onRequest(data, cache, sections)?.let {
                    return it
                }
                info("$type 节点 $id 无法获取解析值")
                return null
            }
            info("$id 拥有未知节点类型 $type")
        }
        return null
    }

    /**
     * 获取节点解析值并存入缓存
     *
     * @param cache 解析值缓存
     * @param sections 节点池
     * @return 解析值
     */
    fun load(
        cache: HashMap<String, String>? = null,
        sections: ConfigurationSection? = null
    ): String? {
        val result = get(cache, sections)
        result?.let { cache?.put(id, it) }
        return result
    }
}