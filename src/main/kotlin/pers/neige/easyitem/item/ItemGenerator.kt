package pers.neige.easyitem.item

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta
import pers.neige.easyitem.manager.ConfigManager.config
import pers.neige.easyitem.manager.ItemManager
import pers.neige.easyitem.utils.ItemUtils.coverWith
import pers.neige.easyitem.utils.ItemUtils.toItemTag
import pers.neige.easyitem.utils.SectionUtils.parseSection
import pers.neige.neigeitems.item.ItemConfig
import pers.neige.neigeitems.utils.ConfigUtils.clone
import pers.neige.neigeitems.utils.ConfigUtils.coverWith
import pers.neige.neigeitems.utils.ConfigUtils.loadFromString
import pers.neige.neigeitems.utils.ConfigUtils.loadGlobalSections
import pers.neige.neigeitems.utils.ConfigUtils.saveToString
import pers.neige.neigeitems.utils.ConfigUtils.toMap
import pers.neige.neigeitems.utils.LangUtils.sendLang
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.ItemTagList
import taboolib.module.nms.getItemTag
import java.io.File
import java.util.*

@RuntimeDependencies(
    RuntimeDependency(
        "!com.alibaba.fastjson2:fastjson2-kotlin:2.0.9",
        test = "!com.alibaba.fastjson2.filter.Filter"
    )
)
/**
 * 物品生成器
 *
 * @property config 物品基础配置
 * @constructor 根据物品基础配置构建物品生成器
 */
class ItemGenerator(config: ItemConfig) {
    val itemConfig: ItemConfig = config

    /**
     * 获取物品ID
     */
    val id: String

    /**
     * 获取物品所在文件
     */
    val file: File

    /**
     * 获取物品原配置
     */
    val originConfigSection: ConfigurationSection

    /**
     * 获取物品解析后配置(经过继承和全局节点调用)
     */
    val configSection: ConfigurationSection

    /**
     * 获取解析后物品配置文本
     */
    val configString: String

    /**
     * 获取物品节点配置
     */
    val sections: ConfigurationSection?

    /**
     * 获取不包含节点配置的物品配置
     */
    val itemSection: ConfigurationSection

    /**
     * 获取不包含节点配置的物品配置
     */
    val itemSectionString: String

    /**
     * 获取解析后物品配置文本哈希值
     */
    val hashCode: Int

    val material: Material?
    val damage: Short?
    val enchantments: Map<Enchantment, Int>?
    val custommodeldata: Int?
    val name: String?
    val lore: List<String>?
    val illegalName: ItemTagData?
    val illegalLore: ItemTagData?
    val unbreakable: Boolean?
    val hideflags: Array<ItemFlag>?
    val color: Color?
    val nbt: ItemTag?

    init {
        id = itemConfig.id
        file = itemConfig.file
        originConfigSection = itemConfig.configSection ?: YamlConfiguration() as ConfigurationSection
        configSection = loadGlobalSections(inherit((YamlConfiguration() as ConfigurationSection), originConfigSection))
        configString = configSection.saveToString(id)
        sections = configSection.getConfigurationSection("sections")
        itemSection = this.configSection.clone().also {
            it.set("sections", null)
        }
        itemSectionString = this.itemSection.saveToString(id)
        hashCode = configString.hashCode()
        val cache = HashMap<String, String>()
        val sections = sections
        val configString = itemSectionString.parseSection(cache, sections)
        val configSection = configString.loadFromString(id) ?: YamlConfiguration()

        material = when {
            configSection.contains("material") -> {
                configSection.getString("material")?.let { Material.matchMaterial(it.uppercase(Locale.getDefault())) }
            }
            else -> null
        }
        damage = when {
            configSection.contains("damage") -> {
                configSection.getInt("damage").toShort()
            }
            else -> null
        }
        enchantments = when {
            configSection.contains("enchantments") -> {
                HashMap<Enchantment, Int>().also{ map ->
                    // 获取所有待设置附魔
                    val enchantSection = configSection.getConfigurationSection("enchantments")
                    // 遍历添加
                    enchantSection?.getKeys(false)?.forEach {
                        val level = enchantSection.getInt(it)
                        val enchant = Enchantment.getByName(it.uppercase(Locale.getDefault()))
                        // 判断等级 && 附魔名称 是否合法
                        if (level > 0 && enchant != null) {
                            // 添加附魔
                            map[enchant] = level
                        }
                    }
                }
            }
            else -> null
        }
        custommodeldata = when {
            configSection.contains("custommodeldata") -> {
                configSection.getInt("custommodeldata")
            }
            else -> null
        }
        name = when {
            configSection.contains("name") -> {
                configSection.getString("name")?.also { ChatColor.translateAlternateColorCodes('&', it) }
            }
            else -> null
        }
        lore = when {
            configSection.contains("lore") -> {
                val originLores = configSection.getStringList("lore")
                ArrayList<String>().also {
                    for (i in originLores.indices) {
                        val lores = ChatColor.translateAlternateColorCodes('&', originLores[i]).split("\n")
                        it.addAll(lores)
                    }
                }
            }
            else -> null
        }
        illegalName = when {
            configSection.contains("illegalName") -> {
                ItemTagData(configSection.getString("illegalName"))
            }
            else -> null
        }
        illegalLore = when {
            configSection.contains("illegalLore") -> {
                ItemTagList().also { itemTagList ->
                    configSection.getStringList("illegalLore").forEach {
                        itemTagList.add(ItemTagData(it))
                    }
                }
            }
            else -> null
        }
        unbreakable = when {
            configSection.contains("unbreakable") -> {
                configSection.getBoolean("unbreakable")
            }
            else -> null
        }
        hideflags = when {
            configSection.contains("hideflags") -> {
                val flags = configSection.getStringList("hideflags")
                ArrayList<ItemFlag>().also {
                    for (value in flags) {
                        try {
                            it.add(ItemFlag.valueOf(value))
                        } catch (_: IllegalArgumentException) {}
                    }
                }.toTypedArray()
            }
            else -> null
        }
        color = when {
            configSection.contains("color") -> {
                Color.fromRGB(when (val colorString = configSection.get("color")) {
                    is String -> colorString.toIntOrNull(16) ?: 0
                    else -> colorString.toString().toIntOrNull() ?: 0
                }.coerceAtLeast(0).coerceAtMost(0xFFFFFF))
            }
            else -> null
        }
        nbt = when {
            configSection.contains("nbt") -> {
                configSection.getConfigurationSection("nbt")?.toMap()?.toItemTag()
            }
            else -> null
        }
    }

    private fun inherit(configSection: ConfigurationSection, originConfigSection: ConfigurationSection): ConfigurationSection {
        // 检测是否需要进行继承
        if (originConfigSection.contains("inherit") == true) {
            // 检测进行全局继承/部分继承
            when (val inheritInfo = originConfigSection.get("inherit")) {
                is MemorySection -> {
                    /**
                     * 指定多个ID, 进行部分继承
                     * @variable key String 要进行继承的节点ID
                     * @variable value String 用于获取继承值的模板ID
                     */
                    inheritInfo.getKeys(true).forEach { key ->
                        // 获取模板ID
                        val value = inheritInfo.get(key)
                        // 检测当前键是否为末级键
                        if (value is String) {
                            // 获取模板
                            val currentSection = ItemManager.getOriginConfig(value)
                            // 如果存在对应模板且模板存在对应键, 进行继承
                            if (currentSection != null && currentSection.contains(key)) {
                                configSection.set(key, currentSection.get(key))
                            }
                        }
                    }
                }
                is String -> {
                    // 仅指定单个模板ID，进行全局继承
                    ItemManager.getOriginConfig(inheritInfo)?.let { inheritConfigSection ->
                        configSection.coverWith(inheritConfigSection)
                    }
                }
                is List<*> -> {
                    // 顺序继承, 按顺序进行覆盖式继承
                    for (templateId in inheritInfo) {
                        // 逐个获取模板
                        ItemManager.getOriginConfig(templateId as String)?.let { currentSection ->
                            // 进行模板覆盖
                            configSection.coverWith(currentSection)
                        }
                    }
                }
            }
        }
        // 覆盖物品配置
        configSection.coverWith(originConfigSection)
        return configSection
    }

    /**
     * 生成物品, 生成失败则返回null
     *
     * @param player 用于解析内容的玩家
     * @param data 指向数据
     * @return 生成的物品, 生成失败则返回null
     */
    fun getItemStack(): ItemStack? {
        // Debug信息
        if (config.getBoolean("Main.Debug")) print(configString)
        if (config.getBoolean("Main.Debug") && sections != null) print(sections.saveToString("$id-sections"))
        material?.also {
            // 构建物品
            val itemStack = ItemStack(material)
            // 设置子ID/损伤值
            damage?.let { itemStack.durability = it }
            // 设置物品附魔
            enchantments?.forEach { (enchant, level) ->
                itemStack.addUnsafeEnchantment(enchant, level)
            }
            // 获取ItemMeta
            val itemMeta = itemStack.itemMeta
            // 设置CustomModelData
            custommodeldata?.let {
                try {
                    itemMeta?.setCustomModelData(it)
                } catch (_: NoSuchMethodError) {}
            }
            // 设置物品名
            name?.let { itemMeta?.setDisplayName(it) }
            // 设置Lore
            lore?.let { itemMeta?.lore = it }
            // 设置是否无法破坏
            unbreakable?.let { itemMeta?.isUnbreakable = it }
            // 设置ItemFlags
            hideflags?.let { itemMeta?.addItemFlags(*it) }
            // 设置物品颜色
            color?.let {
                when (itemMeta) {
                    is LeatherArmorMeta -> itemMeta.setColor(it)
                    is MapMeta -> itemMeta.color = it
                    is PotionMeta -> itemMeta.color = it
                }
            }
            itemStack.itemMeta = itemMeta
            // 设置物品NBT
            val itemTag = itemStack.getItemTag()
            // 设置物品名
            illegalName?.let {
                itemTag.computeIfAbsent("display") { ItemTag() }.asCompound()?.set("Name", it)
            }
            // 设置Lore
            illegalLore?.let {
                itemTag.computeIfAbsent("display") { ItemTag() }.asCompound()?.set("Lore", it)
            }
            // NBT覆盖
            nbt?.let {itemTag.coverWith(it)}
            itemTag.saveTo(itemStack)
            return itemStack
        } ?: let {
            Bukkit.getConsoleSender().sendLang("Messages.invalidMaterial", mapOf(
                Pair("{itemID}", id),
                Pair("{material}", configSection.getString("material") ?: "")
            ))
        }
        return null
    }
}
