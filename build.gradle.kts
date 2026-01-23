plugins {
	id("fabric-loom") version "1.15-SNAPSHOT"
	id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

version = "${project.property("mod_version")}"

base.archivesName.set(project.property("archives_base_name") as String)

repositories {
	exclusiveContent {
		forRepository {
			maven("https://maven.terraformersmc.com/")
		}
		filter {
			includeGroup("com.terraformersmc")
		}
	}

	exclusiveContent {
		forRepository {
			maven("https://api.modrinth.com/maven")
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

loom {
	accessWidenerPath = file("src/main/resources/divorcesteal.accesswidener")

	splitEnvironmentSourceSets()

	mods {
		create("divorcesteal") {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	// Fabric API
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

	// Core mod
	modImplementation("maven.modrinth:pneumono_core:${property("core_version")}")

	// ModMenu
	modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

tasks {
	processResources {
		inputs.property("version", project.property("mod_version"))

		filesMatching("fabric.mod.json") {
			expand(
				mutableMapOf(
					"version" to project.property("mod_version")
				)
			)
		}
	}

	withType<JavaCompile> {
		options.release.set(21)
	}

	java {
		withSourcesJar()
	}

	jar {
		from("LICENSE") {
			rename {"${it}_${base.archivesName.get()}"}
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}