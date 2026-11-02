import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

subprojects {
    plugins.withId("com.android.application") {
        extensions.configure(ApplicationExtension::class.java) {
            val label = hostAppLabel(project.projectDir)
            defaultConfig.resValue("string", "self_provisioner_accessibility_label", "${label}_Self_Provisioner")
        }
    }
}

fun hostAppLabel(projectDir: java.io.File): String {
    val manifest = projectDir.resolve("src/main/AndroidManifest.xml")
    val manifestText = if (manifest.isFile) manifest.readText() else ""
    val applicationTag = Regex("""<application\b[^>]*>""").find(manifestText)?.value.orEmpty()
    val labelRef = Regex("""android:label\s*=\s*"([^"]+)"""")
        .find(applicationTag)
        ?.groupValues
        ?.get(1)
    val stringName = when {
        labelRef == null -> "app_name"
        labelRef.startsWith("@string/") -> labelRef.removePrefix("@string/")
        labelRef.startsWith("@") -> "app_name"
        else -> return decodeXml(labelRef).ifBlank { "App" }
    }
    val values = projectDir.resolve("src/main/res/values")
    val pattern = Regex("""<string\b[^>]*\bname\s*=\s*"${Regex.escape(stringName)}"[^>]*>([^<]*)</string>""")
    values.listFiles()?.filter { it.extension == "xml" }?.forEach { file ->
        val value = pattern.find(file.readText())?.groupValues?.get(1)?.let(::decodeXml)?.trim().orEmpty()
        if (value.isNotEmpty()) return value
    }
    return "App"
}

fun decodeXml(value: String): String = value
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&apos;", "'")
