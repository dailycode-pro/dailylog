plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm {
        mainRun {
            mainClass.set("pro.dailycode.dailylog.testapp.MainKt")
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":library"))
        }
    }
}
