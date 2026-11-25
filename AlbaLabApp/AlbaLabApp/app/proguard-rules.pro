# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# ---- Alba Lab: red (Retrofit + Gson) ----
# Gson usa reflexión sobre los DTO: sin estas reglas R8 renombra los campos y el JSON no se mapea.
-keep class com.corpaliv.albalab.data.remote.dto.** { *; }
-keepclassmembers class com.corpaliv.albalab.data.remote.dto.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Retrofit (interfaces de servicio y funciones suspend)
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep interface com.corpaliv.albalab.data.remote.AuthApi { *; }
-keep interface com.corpaliv.albalab.data.remote.ContenidosApi { *; }
-keep interface com.corpaliv.albalab.data.remote.VinculacionApi { *; }
-keep interface com.corpaliv.albalab.data.remote.IndicadoresApi { *; }
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Room (entidades usadas por KSP) y trazas legibles
-keep class com.corpaliv.albalab.data.local.** { *; }
-keepattributes SourceFile,LineNumberTable
