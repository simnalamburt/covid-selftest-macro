plugins {
	id("com.android.application")
	kotlin("android")
	id("androidx.navigation.safeargs")
}

android {
	compileSdk = 30
	buildToolsVersion = "30.0.2"
	
	defaultConfig {
		applicationId = "com.lhwdev.selfTestMacro"
		minSdk = 21
		targetSdk = 30
		versionCode = 2000
		versionName = "3.0"
		
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
	}
	
	buildFeatures {
		compose = true
	}
	
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	
	kotlinOptions {
		freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=compatibility"
		jvmTarget = "1.8"
	}
	
	composeOptions {
		kotlinCompilerExtensionVersion = "1.0.0-beta09"
	}
}


dependencies {
	implementation(project(":api"))
	implementation(project(":app-serialization")) // workaround for compose + serialization
	
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	implementation(kotlin("stdlib"))
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
	
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
	
	val compose = "1.0.0-beta09" // also kotlinCompilerExtensionVersion, app-serialization/version
	implementation("androidx.compose.ui:ui:$compose")
	implementation("androidx.compose.ui:ui-tooling:$compose")
	implementation("androidx.compose.foundation:foundation:$compose")
	implementation("androidx.compose.material:material:$compose")
	implementation("androidx.activity:activity-compose:1.3.0-beta02")
	
	val accompanist = "0.12.0"
	implementation("com.google.accompanist:accompanist-insets:$accompanist")
	implementation("com.google.accompanist:accompanist-insets-ui:$accompanist")
	implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist")
	
	implementation("androidx.appcompat:appcompat:1.3.0")
	implementation("androidx.core:core-ktx:1.5.0")
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.2")
	// androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}
