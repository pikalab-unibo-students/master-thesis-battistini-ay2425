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

    api(libs.kotlin.coroutines)
    api(libs.ktor.network)

    implementation(libs.bundles.kotlin.testing)
    implementation(libs.bundles.kotlin.logging)
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

tasks.register<JavaExec>("runDomesticRobot") {
    description = "Run the domestic robot application."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.domesticrobot.DomesticRobotRunnerKt"
}

tasks.register<JavaExec>("analyzeIndex") {
    description = "Show the logs stored in the external server."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.evaluation.scripts.AnalyzeIndexKt"
}
