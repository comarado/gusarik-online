pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GusarikOnline"
include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":engine:game")
include(":engine:scoring")
include(":engine:ai")
include(":engine:validation")
include(":network:firebase")
include(":network:realtime")
include(":feature:auth")
include(":feature:menu")
include(":feature:lobby")
include(":feature:game")
include(":feature:history")
include(":feature:stats")
include(":feature:settings")
include(":feature:chat")
