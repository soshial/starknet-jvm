plugins {
    application
}

dependencies {
    testImplementation(project(mapOf("path" to ":lib")))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

val buildCrypto = task<Exec>("BuildCrypto") {
    commandLine("${project.projectDir}/build_crypto.sh")
}

tasks.test{
    useJUnitPlatform()

    val jarPath = file("${rootDir}/java/src/test/resources/starknet.jar").absolutePath
    systemProperty("com.swmansion.starknet.library.jarPath", jarPath)
}

application {}
