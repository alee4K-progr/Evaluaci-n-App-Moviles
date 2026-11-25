import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

// Credenciales de firma: archivo `keystore.properties` en la raíz del proyecto (NO se sube a Git).
// Ver docs/GENERAR_APK_RELEASE.md.
val keystoreProps = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val firmaDisponible = keystoreProps.getProperty("storeFile") != null

// URL del api-gateway (puerto 9090), puerta de entrada a ms-admin, ms-contenidos y ms-vinculacion.
// Se puede cambiar con  -Palbalab.gatewayUrl=http://IP:9090/  (o en gradle.properties).
// 10.0.2.2 es la dirección del PC anfitrión vista desde el emulador de Android.
val gatewayUrl = (project.findProperty("albalab.gatewayUrl") as String?) ?: "http://10.0.2.2:9090/"

android {
    namespace = "com.corpaliv.albalab"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.corpaliv.albalab"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GATEWAY_BASE_URL", "\"$gatewayUrl\"")
        // API pública externa de indicadores económicos de Chile (dólar, UF).
        buildConfigField("String", "INDICADORES_BASE_URL", "\"https://mindicador.cl/\"")
    }

    signingConfigs {
        if (firmaDisponible) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Ofuscación y reducción de código/recursos para el APK de entrega.
            isMinifyEnabled = true
            isShrinkResources = true
            if (firmaDisponible) signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

jacoco {
    toolVersion = "0.8.12"
}

// ---- Cobertura (JaCoCo) de la lógica: dominio, seguridad, red, repositorios y ViewModels ----
// Se excluyen la interfaz Compose y las clases dependientes de la plataforma Android (cámara, notificaciones,
// DataStore, Room generado), que se validan manualmente en el emulador. Ver docs/DOCUMENTACION_TECNICA.md.
val clasesLogica = listOf(
    "**/domain/**", "**/security/**", "**/data/remote/**", "**/data/repository/**", "**/viewmodel/**"
)
val clasesExcluidas = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/*\$\$*", "**/*\$Companion*",
    "**/data/remote/dto/**", "**/data/repository/AlmacenFotos*", "**/viewmodel/Fabrica*", "**/viewmodel/PerfilViewModel*"
)
val clasesKotlin = layout.buildDirectory.dir("tmp/kotlin-classes/debug").map { dir ->
    fileTree(dir) {
        include(clasesLogica)
        exclude(clasesExcluidas)
    }
}
val datosEjecucion = fileTree(layout.buildDirectory) {
    include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Informe de cobertura de las pruebas unitarias (build/reports/jacoco/jacocoTestReport/html/index.html)"
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(clasesKotlin)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(datosEjecucion)
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "verification"
    description = "Falla si la cobertura de instrucciones de la lógica es menor a 80 %"
    dependsOn("jacocoTestReport")
    classDirectories.setFrom(clasesKotlin)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(datosEjecucion)
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

dependencies {
    // Base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // Persistencia local: Room (datos) + DataStore (preferencias / sesión)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Red: Retrofit + OkHttp + Gson (microservicio propio y API externa)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)

    // Carga de imágenes (foto de perfil)
    implementation(libs.coil.compose)

    // Pruebas
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
