import java.time.Year

plugins {
    id("fabric-loom") version "0.7-SNAPSHOT"
    id("org.cadixdev.licenser") version("0.5.1")
}

// versions
val minecraftVersion = "21w13a"
val yarnBuild = "43"
val loaderVersion = "0.11.3"
val fabricVersion = "0.32.6+1.17"
//val modmenuVersion = "1.16.8"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

base {
    archivesBaseName = "JarVR"
}

version = "0.0.0${getVersionDecoration()}"
group = "dev.joezwet"

repositories {
//    maven("https://maven.terraformersmc.com/releases/") {
//        content { includeGroup("com.terraformersmc") }
//    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.$yarnBuild:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
//    modImplementation("com.terraformersmc:modmenu:$modmenuVersion")
}

tasks.processResources {
    inputs.property("version", project.version)

    from(sourceSets.main.get().resources.srcDirs) {
        include("fabric.mod.json")
        expand(mutableMapOf("version" to project.version))
    }

    from(sourceSets.main.get().resources.srcDirs) {
        exclude("fabric.mod.json")
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.jar {
    from("LICENSE")
}

license {
    header = project.file("LICENSE_HEADER")
    include("**/dev/joezwet/**/*.java")
    include("build.gradle.kts")
    ext {
        set("year", Year.now().value)
        set("company", "Joe van der Zwet")
    }
}

// inspired by https://github.com/TerraformersMC/GradleScripts/blob/2.0/ferry.gradle
fun getVersionDecoration(): String {
    if(project.hasProperty("release")) return ""

    var version = "+build"
    val branch = "git branch --show-current".execute()
    if(branch.isNotEmpty() && branch != "main") {
        version += ".${branch}"
    }
    val commitHashLines = "git rev-parse --short HEAD".execute()
    if(commitHashLines.isNotEmpty()) {
        version += ".${commitHashLines}"
    }
    return version
}

// from https://discuss.gradle.org/t/how-to-run-execute-string-as-a-shell-command-in-kotlin-dsl/32235/5
fun String.execute(workingDir: File = projectDir): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}