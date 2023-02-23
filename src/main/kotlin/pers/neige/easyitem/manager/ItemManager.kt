package pers.neige.easyitem.manager

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import pers.neige.easyitem.EasyItem.plugin
import pers.neige.easyitem.item.ItemGenerator
import pers.neige.easyitem.utils.ItemUtils.invalidNBT
import pers.neige.easyitem.utils.ItemUtils.toMap
import pers.neige.neigeitems.item.ItemConfig
import pers.neige.neigeitems.utils.ConfigUtils.clone
import taboolib.module.nms.getItemTag
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 物品管理器
 *
 * @constructor 构建物品管理器
 */
object ItemManager : ItemConfigManager() {
    /**
     * 获取所有物品生成器
     */
    val items: ConcurrentHashMap<String, ItemGenerator> = ConcurrentHashMap<String, ItemGenerator>()

    /**
     * 获取物品总数
     */
    val itemAmount get() = itemIds.size

    init {
        // 初始化所有物品生成器
        loadItems()
    }

    /**
     * 初始化所有物品生成器
     */
    private fun loadItems() {
        for ((id, itemConfig) in itemConfigs) {
            items[id] = ItemGenerator(itemConfig)
        }
    }

    /**
     * 添加物品生成器
     * @param itemGenerator 待添加物品生成器
     */
    private fun addItem(itemGenerator: ItemGenerator) {
        itemConfigs[itemGenerator.id] = itemGenerator.itemConfig
        items[itemGenerator.id] = itemGenerator
    }

    /**
     * 重载物品管理器
     */
    fun reload() {
        reloadItemConfigs()
        items.clear()
        loadItems()
    }

    /**
     * 获取物品原始Config的克隆
     *
     * @param id 物品ID
     * @return 物品原始Config的克隆
     */
    fun getOriginConfig(id: String): ConfigurationSection? {
        return itemConfigs[id]?.configSection?.clone()
    }

    /**
     * 获取物品原始Config
     *
     * @param id 物品ID
     * @return 物品原始Config
     */
    fun getRealOriginConfig(id: String): ConfigurationSection? {
        return itemConfigs[id]?.configSection
    }

    /**
     * 获取物品生成器
     *
     * @param id 物品ID
     * @return 物品生成器
     */
    fun getItem(id: String): ItemGenerator? {
        return items[id]
    }

    /**
     * 获取物品
     *
     * @param id 物品ID
     * @return 物品
     */
    fun getItemStack(id: String): ItemStack? {
        return items[id]?.getItemStack()
    }


    /**
     * 是否存在对应ID的物品
     *
     * @param id 物品ID
     * @return 是否存在对应ID的物品
     */
    fun hasItem(id: String): Boolean {
        return items.containsKey(id)
    }

    /**
     * 保存物品
     *
     * @param itemStack 保存物品
     * @param id 物品ID
     * @param path 保存路径
     * @param cover 是否覆盖
     * @return 1 保存成功; 0 ID冲突; 2 你保存了个空气
     */
    fun saveItem(itemStack: ItemStack, id: String, path: String = "$id.yml", cover: Boolean): Int {
        // 检测是否为空气
        if (itemStack.type != Material.AIR) {
            // 获取路径文件
            val file = File(plugin.dataFolder, "${File.separator}Items${File.separator}$path")
            if(!file.exists()) { file.createNewFile() }
            val config = YamlConfiguration.loadConfiguration(file)
            // 检测节点是否存在
            if (!hasItem(id) || cover) {
                // 创建物品节点
                val configSection = config.createSection(id)
                // 设置物品材质
                configSection.set("material", itemStack.type.toString())
                // 设置子ID/损伤值
                if (itemStack.durability > 0) {
                    configSection.set("damage", itemStack.durability)
                }
                // 如果物品有ItemMeta
                if (itemStack.hasItemMeta()) {
                    // 获取ItemMeta
                    val itemMeta = itemStack.itemMeta
                    // 获取物品NBT
                    val itemNBT = itemStack.getItemTag()
                    // 获取显示信息
                    val display = itemNBT["display"]
                    itemNBT.remove("display")
                    // 设置CustomModelData
                    try {
                        if (itemMeta?.hasCustomModelData() == true) {
                            configSection.set("custommodeldata", itemMeta.customModelData)
                        }
                    } catch (error: NoSuchMethodError) {}
                    // 设置物品名
                    if (itemMeta?.hasDisplayName() == true) {
                        configSection.set("name", itemMeta.displayName)
                    }
                    // 设置Lore
                    if (itemMeta?.hasLore() == true) {
                        configSection.set("lore", itemMeta.lore)
                    }
                    // 设置是否无法破坏
                    if (itemMeta?.isUnbreakable == true) {
                        configSection.set("unbreakable", itemMeta.isUnbreakable)
                    }
                    // 设置物品附魔
                    if (itemMeta?.hasEnchants() == true) {
                        val enchantSection = configSection.createSection("enchantments")
                        for ((enchant, level) in itemMeta.enchants) {
                            enchantSection.set(enchant.name, level)
                        }
                    }
                    // 设置ItemFlags
                    itemMeta?.itemFlags?.let{
                        if (it.isNotEmpty()) {
                            configSection.set("hideflags", it.map { flag -> flag.name })
                        }
                    }
                    // 设置物品颜色
                    display?.asCompound()?.let {
                        it["color"]?.asInt()?.let { color ->
                            configSection.set("color", color.toString(16).uppercase(Locale.getDefault()))
                        }
                    }
                    // 非法Name/Lore检测
                    display?.asCompound()?.also {
                        // Name/Lore格式化
                        val itemClone = itemStack.clone()
                        val cloneMeta = itemClone.itemMeta
                        cloneMeta?.setDisplayName(cloneMeta.displayName)
                        cloneMeta?.lore = cloneMeta?.lore
                        itemClone.itemMeta = cloneMeta
                        // 格式化后的display
                        val cloneDisplay = itemClone.getItemTag()["display"]?.asCompound()
                        // 非法Name
                        it["Name"]?.asString()?.let { name ->
                            if (name != cloneDisplay?.get("Name")?.asString()) {
                                configSection.set("illegalName", name)
                            }
                        }
                        // 非法Lore
                        it["Lore"]?.asList()?.let { lore ->
                            val cloneLore = cloneDisplay?.get("Lore")?.asList()
                            var illegal = false
                            if (cloneLore?.size != lore.size) {
                                illegal = true
                            }
                            for (i in 0 until lore.size) {
                                if (lore[i].asString() != cloneLore?.get(i)?.asString()) {
                                    illegal = true
                                }
                            }
                            if (illegal) {
                                configSection.set("illegalLore", lore.map { it.asString()} )
                            }
                        }
                    }
                    // 设置物品NBT
                    if (!itemNBT.isEmpty()) {
                        configSection.set("nbt", itemNBT.toMap(invalidNBT))
                    }
                }
                // 保存文件
                config.save(file)
                // 物品保存好了, 信息加进ItemManager里
                addItem(ItemGenerator(ItemConfig(id, file, config)))
                if (cover) return 0
                return 1
            }
            return 0
        }
        return 2
    }
}