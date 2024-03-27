package com.example.utils

const val GOOGLE_AUTH_HOST = "accounts.google.com"
const val STUDIO_YOUTUBE_URL = "https://studio.youtube.com"
const val ARS_GRST_PATH = "/ars/grst"
const val SIGNIN_CHALLENGE_SELECTION_PATH = "/signin/challenge/selection"
const val V1_API_PATH = "/api/v1"

const val SID_COOKIE = "SID"
const val HSID_COOKIE = "HSID"
const val SSID_COOKIE = "SSID"
const val APISID_COOKIE = "APISID"
const val SAPISID_COOKIE = "SAPISID"
const val SECURE_1PSID_COOKIE = "__Secure-1PSID"
const val SECURE_3PSID_COOKIE = "__Secure-3PSID"

val COOKIES_LIST = arrayOf(
    SID_COOKIE,
    HSID_COOKIE,
    SSID_COOKIE,
    APISID_COOKIE,
    SAPISID_COOKIE,
    SECURE_1PSID_COOKIE,
    SECURE_3PSID_COOKIE
)

val GEOS_LIST = linkedMapOf(
    "ALLGEO" to "Все ГЕО",
    "AU" to "Австралия",
    "BR" to "Бразилия",
    "DE" to "Германия",
    "ES" to "Испания",
    "FR" to "Франция",
    "GB" to "Великобритания",
    "IN" to "Индия",
    "IT" to "Италия",
    "PL" to "Польша",
    "PT" to "Португалия",
    "RU" to "Россия",
    "US" to "США"
)

val TOKEN_DECIMAL = 6