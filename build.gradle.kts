var mainClass = "de.timolia.legacycombatsimulation.LegacyCombatSimulation"

group = System.getenv("CI_PROJECT_NAMESPACE")?.replace("/", ".") ?: "local"
version = "git-" + (System.getenv("CI_COMMIT_REF_NAME") ?: "local")

plugins {
    java
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.5.11"
}

// Load local env file
val localEnvFile = File(
    "${System.getProperties().getProperty("user.home")}${File.separator}.gradle",
    "env-timolia.local.gradle.kts"
)

val isLive = System.getenv("IS_PIPELINE_LIVE") != null

if (localEnvFile.exists()) {
    // Set project extras local
    apply(from = localEnvFile.path)
} else {
    // Pipeline
    if (isLive) {
        // Live
        project.extra.set("mavenToken", System.getenv("MAVEN_LIVE_TOKEN") as String)
        project.extra.set("mavenUser", System.getenv("MAVEN_LIVE_USER") as String)
        project.extra.set("repository", System.getenv("MAVEN_LIVE_REPO") as String)
        project.extra.set("mavenUrl", System.getenv("MAVEN_LIVE_URL") as String)
    } else {
        // Dev
        project.extra.set("mavenToken", System.getenv("MAVEN_DEV_TOKEN") as String)
        project.extra.set("mavenUser", System.getenv("MAVEN_DEV_USER") as String)
        project.extra.set("repository", System.getenv("MAVEN_DEV_REPO") as String)
        project.extra.set("mavenUrl", System.getenv("MAVEN_DEV_URL") as String)
    }
}
dependencies {
    paperweight.devBundle("io.papermc.paper", "1.20.1-R0.1-SNAPSHOT")
    compileOnly(group = "com.comphenix.protocol", name = "ProtocolLib", version = "5.1.0")
}

configurations.all {
    resolutionStrategy.failOnVersionConflict()
    resolutionStrategy.preferProjectModules()
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    withType<PublishToMavenRepository> {
        dependsOn(reobfJar)
    }
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

// Replace plugin.yml
tasks.withType<ProcessResources> {
    filesMatching("plugin.yml") {
        expand(
            "VERSION" to System.getenv().getOrDefault("CI_JOB_ID", "unknown"),
            "MAINCLASS" to mainClass,
            "BUILT" to System.getenv().getOrDefault("CI_COMMIT_TIMESTAMP", "unknown"),
            "BRANCH" to System.getenv().getOrDefault("CI_COMMIT_REF_NAME", "unknown"),
            "PIPELINE" to System.getenv().getOrDefault("CI_PIPELINE_ID", "unknown"),
            "PIPELINEURL" to System.getenv().getOrDefault("CI_PIPELINE_URL", "unknown"),
            "COMMIT" to System.getenv().getOrDefault("CI_COMMIT_SHORT_SHA", "unknown"),
            "PROJECTURL" to System.getenv().getOrDefault("CI_PROJECT_URL", "unknown")
        )
    }
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

publishing {
    publications {
        register("release", MavenPublication::class) {
            artifact(tasks.jar.get().archiveFile.get()).classifier = "obfuscated"
            pom {
                properties.put("CI_COMMIT_REF_NAME", System.getenv("CI_COMMIT_REF_NAME") ?: "local")
                properties.put("CI_PIPELINE_ID", System.getenv("CI_PIPELINE_ID") ?: "local")
                properties.put("CI_PROJECT_NAME", System.getenv("CI_PROJECT_NAME") ?: "local")
            }
            from(components["java"])
        }

        // use credentials from the start of the file to publish, if live branch in the live repo, if dev branch in dev repo
        repositories {
            maven {
                url = uri("${project.extra["mavenUrl"] as String}/${project.extra["repository"] as String}")
                name = "Reposilite"
                authentication {
                    create<BasicAuthentication>("basic")
                }
                credentials {
                    username = project.extra["mavenUser"] as String
                    password = project.extra["mavenToken"] as String
                }
            }
        }

        // if live pipeline we want it also in the dev repo
        if (isLive) {
            repositories {
                maven {
                    url = uri("${System.getenv("MAVEN_DEV_URL") as String}/${System.getenv("MAVEN_DEV_REPO") as String}")
                    name = "Reposilite-Dev"
                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                    credentials {
                        username = System.getenv("MAVEN_DEV_USER") as String
                        password = System.getenv("MAVEN_DEV_TOKEN") as String
                    }
                }
            }
        }
    }
}
