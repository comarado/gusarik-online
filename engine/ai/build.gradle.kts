plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":engine:validation"))

    testImplementation(libs.junit)
}
