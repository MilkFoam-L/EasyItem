import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val taboolib_version: String by project

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/public")
    maven("https://jitpack.io")
    maven("https://r.irepo.space/maven/")
    maven {
        url = uri("http://ptms.ink:8081/repository/releases/")
        isAllowInsecureProtocol = true
    }
    maven("https://repo.tabooproject.org/storages/public/releases")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.15.113")

    // fastjson2
    compileOnly("com.alibaba.fastjson2:fastjson2-kotlin:2.0.25")

    // taboolib
    implementation("io.izzel.taboolib:common:$taboolib_version")
    implementation("io.izzel.taboolib:common-5:$taboolib_version")
    implementation("io.izzel.taboolib:module-chat:$taboolib_version")
    implementation("io.izzel.taboolib:module-configuration:$taboolib_version")
    implementation("io.izzel.taboolib:module-nms:$taboolib_version")
    implementation("io.izzel.taboolib:module-nms-util:$taboolib_version")
    implementation("io.izzel.taboolib:module-metrics:$taboolib_version")
    implementation("io.izzel.taboolib:platform-bukkit:$taboolib_version")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
        // kotlin
        relocate("kotlin.", "pers.neige.neigeitems.libs.kotlin.")
        // taboolib
        relocate("taboolib", "pers.neige.easyitem.taboolib")
        // fastjson2
        relocate("com.alibaba.fastjson2", "pers.neige.neigeitems.libs.fastjson2")
    }
    kotlinSourcesJar {
        // include subprojects
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
    build {
        dependsOn(shadowJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

tasks.create("apiJar", Jar::class) {
    dependsOn(tasks.compileJava, tasks.compileKotlin)
    from(tasks.compileJava, tasks.compileKotlin)

    // clean no-class file
    include { it.isDirectory or it.name.endsWith(".class") }
    includeEmptyDirs = false

    archiveClassifier.set("api")
}

tasks.assemble {
    dependsOn(tasks["apiJar"])
}

// 将plugin.yml中的"${version}"替换为插件版本
tasks.register("replaceVersionInPluginYml") {
    doLast {
        val inputFile = File("src/main/resources/plugin.yml")
        val outputFile = File("build/resources/main/plugin.yml")

        val inputText = inputFile.readText()

        val projectVersion = version.toString()
        val replacedText = inputText.replace("\${version}", projectVersion)

        outputFile.writeText(replacedText)
    }
}

tasks.named("assemble") {
    dependsOn("replaceVersionInPluginYml")
}
