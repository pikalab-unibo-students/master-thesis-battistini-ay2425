plugins {
    alias(libs.plugins.kotlinx)
    alias(libs.plugins.ksp)
}

dependencies {
    api(project(":jakta-dsl"))
    api(project(":jakta-bdi"))

    api(libs.tuprolog.core)
    api(libs.tuprolog.theory)
    api(libs.tuprolog.parser.theory)
    api(libs.tuprolog.parser.core)
    api(libs.tuprolog.dsl.theory)
    api(libs.tuprolog.dsl.core)
    api(libs.tuprolog.oop.lib)
    api(libs.tuprolog.solve.classic)

    api(libs.bundles.koin)
    api(libs.bundles.kotlin.logging)
    api(libs.kotlinx.serialization.json)
    ksp(libs.koin.ksp.compiler)
}

kotlin {
    sourceSets.main.configure {
        kotlin.srcDir("build/generated/ksp/src/main/kotlin")
    }
}
