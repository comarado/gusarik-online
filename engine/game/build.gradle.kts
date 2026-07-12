plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":engine:scoring"))
    implementation(project(":engine:validation"))
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}
