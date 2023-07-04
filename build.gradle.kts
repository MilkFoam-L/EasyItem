plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.56"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

val api: String? by project
val lib: String? by project

task("versionCheck") {
    if (api != null) {
        val origin = project.version.toString()
        project.version = "$origin-api"
    } else if (lib != null) {
        val origin = project.version.toString()
        project.version = "$origin-lib"
    }
}

taboolib {
    if (project.version.toString().contains("-api")) {
        options("skip-kotlin-relocate")
    }
    description {
        contributors {
            name("Neige")
        }
        dependencies {
            name("NeigeItems").with("bukkit")
        }
    }
    install(
        "common",
        "common-5",
        "module-chat",
        "module-configuration",
        "module-nms",
        "module-nms-util",
        "module-metrics",
        "platform-bukkit",
    )
    classifier = null
    version = "6.0.11-13"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public") }
    maven { url = uri("https://mvn.lumine.io/repository/maven-public") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.tabooproject.org/storages/public/releases") }
    maven("https://r.irepo.space/maven/")
}

dependencies {
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11902:11902-minimize:mapped")
    compileOnly("ink.ptms.core:v11902:11902-minimize:universal")
    compileOnly(fileTree("libs"))
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.1.0")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.14.20")
    if (project.version.toString().contains("-lib")) {
        taboo(kotlin("stdlib"))
    } else {
        compileOnly(kotlin("stdlib"))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = project.group.toString()
        }
    }
}