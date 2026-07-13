plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":engine:scoring"))
    implementation(project(":engine:validation"))
    implementation(project(":core:domain"))
    implementation(project(":engine:ai"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)

}
