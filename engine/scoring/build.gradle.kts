plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
// 1. Coroutines (for StateFlow and MutableStateFlow)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1") // Or use your project's version catalog variable

    // 2. Dependency Injection (for @Inject and @Singleton)
    implementation("javax.inject:javax.inject:1")
}
