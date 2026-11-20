package com.nicholasnixon.dailychore.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.nicholasnixon.dailychore.R

/*
 * Nunito: a round, friendly font, downloaded by the phone from Google Fonts the first time
 * it's needed (so it doesn't make the app bigger). Until it arrives, or if the phone can't get
 * it, Android's normal font is used instead.
 * Pattern from developer.android.com: "Work with fonts" → downloadable fonts.
 * The certificates (res/values/font_certs.xml) prove the font really comes from Google.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val nunito = GoogleFont("Nunito")

val Nunito = FontFamily(
    Font(googleFont = nunito, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = nunito, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = nunito, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = nunito, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = nunito, fontProvider = provider, weight = FontWeight.ExtraBold)
)

// Start from Material's default sizes and swap in Nunito, with bolder headings and buttons.
private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    displayMedium = base.displayMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    displaySmall = base.displaySmall.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
    bodyMedium = base.bodyMedium.copy(fontFamily = Nunito),
    bodySmall = base.bodySmall.copy(fontFamily = Nunito),
    labelLarge = base.labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    labelSmall = base.labelSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold)
)
