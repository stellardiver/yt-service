package com.example.utils

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.request.*

fun Any.prettyPrint(): String {

    var indentLevel = 0
    val indentWidth = 4

    fun padding() = "".padStart(indentLevel * indentWidth)

    val toString = toString()

    val stringBuilder = StringBuilder(toString.length)

    var i = 0
    while (i < toString.length) {
        when (val char = toString[i]) {
            '(', '[', '{' -> {
                indentLevel++
                stringBuilder.appendLine(char).append(padding())
            }
            ')', ']', '}' -> {
                indentLevel--
                stringBuilder.appendLine().append(padding()).append(char)
            }
            ',' -> {
                stringBuilder.appendLine(char).append(padding())
                // ignore space after comma as we have added a newline
                val nextChar = toString.getOrElse(i + 1) { char }
                if (nextChar == ' ') i++
            }
            else -> {
                stringBuilder.append(char)
            }
        }
        i++
    }

    return stringBuilder.toString()
}

fun formatNumber(num: Int, formattedNumberType: FormattedNumberType): String {

    val type = when (formattedNumberType) {
        FormattedNumberType.VIEW -> "views"
        FormattedNumberType.LIKE -> "likes"
        FormattedNumberType.COMMENT -> "comments"
        FormattedNumberType.SUBSCRIBER -> "subscribers"
    }

    return if (num < 1000) {
        "$num $type"
    } else if (num < 1_000_000) {
        val thousands = num / 1000.0
        "%.1fK $type".format(thousands)
    } else {
        val millions = num / 1_000_000.0
        "%.1fM $type".format(millions)
    }
}

fun findLinks(input: String): List<String> {
    val regex = Regex("""(https?://\S+)""")
    val matchResults = regex.findAll(input)

    return matchResults.map { it.groups[1]?.value.orEmpty() }.toList()
}

fun parseLinks(input: String): List<String> {
    return input.split(";")
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

fun parseStringToIntegerArray(input: String): Array<Int> {
    val numbers = input.split(", ")
        .map { extractNumber(it) }
        .toTypedArray()

    return numbers
}

fun extractNumber(s: String): Int {
    val regex = Regex("\\d{1,3}(\\.\\d{3})*(?!\\d)")
    val matchResult = regex.find(s)
    val numberString = matchResult?.value

    return numberString?.replace(".", "")?.toIntOrNull() ?: 0
}

suspend inline fun <reified T> ApplicationCall.safeReceive(): T {
    val json = this.receiveNullable<String>()
    return Gson().fromJson(json, T::class.java)
}