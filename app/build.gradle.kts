plugins {
	id("com.android.application")
	kotlin("android")
	kotlin("plugin.serialization")
	
	id("app.cash.licensee") version "1.2.0"
}

repositories {
	maven(url = "https://oss.sonatype.org/content/repositories/snapshots") // lottie-compose
}

licensee {
	allow("Apache-2.0")
	allowUrl("http://opensource.org/licenses/BSD-2-Clause") // commonmark
	// allow("MIT")
	
	// see https://github.com/airbnb/lottie-android/issues/1865
	allowDependency(groupId = "com.airbnb.android", artifactId = "lottie", version = "4.1.0")
	allowDependency(groupId = "com.airbnb.android", artifactId = "lottie-compose", version = "4.1.0")
}

tasks.register<Copy>("updateLicenses") {
	dependsOn("licenseeRelease")
	from(File(project.buildDir, "reports/licensee/release/artifacts.json"))
	into(project.file("src/main/res/raw"))
	rename { "open_source_license.json" }
}

android {
	compileSdk = 31
	
	defaultConfig {
		applicationId = "com.lhwdev.selfTestMacro"
		minSdk = 21
		targetSdk = 30
		versionCode = 3000
		versionName = "3.0.0-build01"
		
		// multiDexEnabled = true
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	
	buildTypes {
		named("release") {
			isMinifyEnabled = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
		
		named("debug") {
			applicationIdSuffix = ".preview"
		}
	}
	
	buildFeatures {
		compose = true
	}
	
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	
	kotlinOptions {
		freeCompilerArgs = freeCompilerArgs + listOf(
			"-Xjvm-default=compatibility",
			"-Xopt-in=" + listOf(
				"kotlin.RequiresOptIn",
				"androidx.compose.material.ExperimentalMaterialApi",
				"androidx.compose.ui.ExperimentalComposeUiApi",
				"androidx.compose.animation.ExperimentalAnimationApi"
			).joinToString(separator = ",")
		)
		jvmTarget = "1.8"
	}
	
	composeOptions {
		kotlinCompilerVersion = "1.5.21"
		kotlinCompilerExtensionVersion = "1.0.1"
	}
}


dependencies {
	implementation(project(":app-base"))
	implementation(project(":app-models"))
	implementation(project(":api"))
	implementation(project(":api-base"))
	implementation(project(":utils"))
	
	implementation("com.airbnb.android:lottie-compose:4.1.0")
	
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
	
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
	
	implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")
	
	val compose = "1.0.1" // also kotlinCompilerExtensionVersion, app-serialization/version, app-models
	implementation("androidx.compose.ui:ui:$compose")
	implementation("androidx.compose.ui:ui-tooling:$compose")
	implementation("androidx.compose.foundation:foundation:$compose")
	// implementation("androidx.compose.animation:animation-graphics:$compose")
	implementation("androidx.compose.material:material:$compose")
	implementation("androidx.activity:activity-compose:1.3.1")
	
	val accompanist = "0.17.0" // also in app-base
	implementation("com.google.accompanist:accompanist-insets:$accompanist")
	implementation("com.google.accompanist:accompanist-insets-ui:$accompanist")
	implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist")
	
	implementation("com.halilibo.compose-richtext:richtext-ui-material:0.7.0")
	implementation("com.halilibo.compose-richtext:richtext-commonmark:0.7.0")
	
	implementation("androidx.appcompat:appcompat:1.3.1")
	implementation("androidx.core:core-ktx:1.6.0")
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.3")
	// androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}
