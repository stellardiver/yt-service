package com.example.routes

import com.example.utils.V1_API_PATH
import com.example.models.GmailAccountProduct
import com.example.services.MarketService
import com.example.utils.safeReceive
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
import java.util.*

fun Route.marketParseRouting(config: ApplicationConfig, marketService: MarketService) {

    post("$V1_API_PATH/update_accs_market_products") {

        val accountProducts = call.safeReceive<Array<GmailAccountProduct>>()

        accountProducts.forEach { accountProduct ->
            marketService.saveGmailAccountProduct(accountProduct)
        }

        call.respond(hashMapOf("success" to "true"))
    }

    get("$V1_API_PATH/generate_accs_market_products_analytics") {

        val accountProducts = marketService.getAllGmailAccountProducts()
        val timeSliceRowSize = accountProducts[0].datestampHistory.split(":").size

        println(accountProducts.size)

        val topRow = mutableListOf<String>().apply {
            add("Инфо о продавце")
        }

        for (i in 0..< timeSliceRowSize) {
            topRow.add("Временной отрезок")
        }

        csvWriter().open("test.csv", append = false) {
            writeRow(topRow)
        }

        accountProducts.forEach { accountProduct ->

            val productRow = mutableListOf<String>().apply {
                add(
                    "ID продавца: ${accountProduct.productId}\n" +
                    "Название товара: ${accountProduct.title}\n" +
                    "Ссылка: ${accountProduct.link}\n"
                )
            }

            val rowCost = accountProduct.costHistory.split(":")
            val rowQuantity = accountProduct.quantityHistory.split(":")
            val rowDatestampUnix = accountProduct.datestampHistory.split(":")
            val rowDatestamp = mutableListOf<String>()
            var dayDifference = 0

            rowDatestampUnix.forEach { datestampUnix ->

                val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("ru"))

                rowDatestamp.add(
                    simpleDateFormat.format(datestampUnix.toLong())
                )
            }

            for (i in 0..< timeSliceRowSize) {

                var stringToAdd = ""

                runCatching {

                     stringToAdd = "\nЦена: ${rowCost[i]}$ за штуку\n" +
                            "Дата парсинга: ${rowDatestamp[i]}\n" +
                            "Количество: ${rowQuantity[i]} шт.\n"

                    if (i > 0) {

                        stringToAdd = if (rowQuantity[i - 1].toInt() < rowQuantity[i].toInt()) {
                            stringToAdd.plus(
                                "За последний час продавец добавил новые аккаунты\n"
                            )

                        } else {

                            val hourDifference = rowQuantity[i - 1].toInt() - rowQuantity[i].toInt()

                            dayDifference += hourDifference

                            stringToAdd.plus(
                                "Продано за час: $hourDifference шт.\n"
                            )
                        }
                    }

                    stringToAdd = stringToAdd.plus(
                        "Продано за день: $dayDifference шт.\n"
                    )

                }.onFailure {
                    println(it)
                }

                productRow.add(stringToAdd)

            }

            csvWriter().open("test.csv", append = true) {
                writeRow(
                    productRow
                )
            }
        }

        call.respond(hashMapOf("success" to "true"))
    }
}