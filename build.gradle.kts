// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
true // Needed to make the Suppress annotation work for the plugins block