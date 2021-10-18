plugins {
    kotlin("jvm") version "1.5.31"
    application
}

version = "0.2.0"
group = "io.github.coteji"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.ajalt.clikt:clikt:3.3.0")
    implementation("io.github.coteji:coteji-core:0.2.1")
    implementation("org.slf4j:slf4j-nop:1.7.32") // needed to get rid of slf4j self-logging info
}

application {
    mainClass.set("io.github.coteji.CotejiKt")
}

distributions {
    main {
        this.contents
            .rename("^coteji-cli$", "coteji")
            .rename("coteji-cli\\.bat", "coteji.bat")
    }
}
