package pers.neige.easyitem.utils

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import pers.neige.easyitem.manager.ConfigManager
import pers.neige.easyitem.manager.ConfigManager.pathSeparator
import pers.neige.neigeitems.utils.ConfigUtils.clone
import taboolib.platform.BukkitPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object ConfigUtils {
    /**
     * 深复制ConfigurationSection
     *
     * @return 对应ConfigurationSection的克隆
     */
    @JvmStatic
    fun ConfigurationSection.clone(): ConfigurationSection {
        val tempConfigSection = YamlConfiguration().also { it.options().pathSeparator(pathSeparator) }
        this.getKeys(false).forEach { key ->
            when (val value = this.get(key)) {
                is ConfigurationSection -> tempConfigSection.set(key, value.clone())
                is List<*> -> tempConfigSection.set(key, value.clone())
                else -> tempConfigSection.set(key, value)
            }
        }
        return tempConfigSection
    }

    /**
     * String 转 ConfigurationSection
     * @param id 转换前使用的节点ID
     * @return 转换结果
     */
    @JvmStatic
    fun String.loadFromString(id: String): ConfigurationSection? {
        val tempConfigSection = YamlConfiguration()
        tempConfigSection.options().pathSeparator(pathSeparator)
        tempConfigSection.loadFromString(this)
        return tempConfigSection.getConfigurationSection(id)
    }

    /**
     * 保存默认文件(不进行替换)
     */
    @JvmStatic
    fun BukkitPlugin.saveResourceNotWarn(resourcePath: String) {
        this.getResource(resourcePath.replace('\\', '/'))?.let { inputStream ->
            val outFile = File(this.dataFolder, resourcePath)
            val lastIndex: Int = resourcePath.lastIndexOf(File.separator)
            val outDir = File(this.dataFolder, resourcePath.substring(0, if (lastIndex >= 0) lastIndex else 0))
            if (!outDir.exists()) {
                outDir.mkdirs()
            }
            if (!outFile.exists()) {
                try {
                    var len: Int
                    val fileOutputStream = FileOutputStream(outFile)
                    val buf = ByteArray(1024)
                    while (inputStream.read(buf).also { len = it } > 0) {
                        (fileOutputStream as OutputStream).write(buf, 0, len)
                    }
                    fileOutputStream.close()
                    inputStream.close()
                } catch (ex: IOException) {}
            }
        }
    }
}