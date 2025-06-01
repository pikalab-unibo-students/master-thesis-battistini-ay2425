import java.util.Properties

plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx)
    alias(libs.plugins.ksp)
}

repositories {
    maven("https://maven.tryformation.com/releases") {
        content {
            includeGroup("com.jillesvangurp")
        }
    }
}

dependencies {
    api(project(":jakta-narrative-generation"))
    api(project(":jakta-dsl"))
    api(project(":jakta-plan-generation"))

    api(libs.kotlin.coroutines)
    api(libs.ktor.network)

    implementation(libs.bundles.kotlin.testing)
    implementation(libs.bundles.kotlin.logging)
    implementation(libs.openai)
    implementation(libs.clikt)
    implementation(libs.bundles.koin)
    implementation(libs.ktsearch)
    ksp(libs.koin.ksp.compiler)
}

kotlin {
    sourceSets.main.configure {
        kotlin.srcDir("build/generated/ksp/src/main/kotlin")
    }
}

tasks.register<JavaExec>("runExperiment") {
    val keystoreFile = project.rootProject.file(".env")
    val properties = Properties()
    properties.load(keystoreFile.inputStream())

    environment = mapOf("API_KEY" to properties.getProperty("API_KEY"))
    description = "Run the explorer agent sample with the given experimental config."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.explorer.ExperimentRunnerKt"
}

tasks.register<JavaExec>("replayExperiment") {
    description = "Run the explorer agent sample by reusing already generated responses."
    group = "application"

    val baseExpDir = project.rootProject.projectDir.resolve("jakta-playground")
    val expDir = project.findProperty("expDir") as? String ?: "experiments"
    val additionalArgs =
        mutableListOf<String>().apply {
            add("--exp-dir")
            add(baseExpDir.resolve(expDir).toString())

            if (project.hasProperty("logToFile")) {
                add("--log-to-file")
            }
        }

    args = additionalArgs
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.ExperimentReplayerKt"
}

tasks.register<JavaExec>("matchFile") {
    description = "Run the pattern matcher on a file."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.evaluation.scripts.MatchFileKt"
}

tasks.register<JavaExec>("matchStream") {
    description = "Run the pattern matcher on a stream of TCP data."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.evaluation.scripts.MatchStreamKt"
}

tasks.register<JavaExec>("runBaseline") {
    description = "Run the explorer agent sample with the baseline plans."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.BaselineExplorerKt"
}
