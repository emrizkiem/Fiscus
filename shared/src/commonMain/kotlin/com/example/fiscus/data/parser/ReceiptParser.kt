package com.example.fiscus.data.parser

import kotlinx.datetime.LocalDate

data class ParsedReceipt(
    val amount: Long?,
    val merchant: String?,
    val date: LocalDate?,
)

object ReceiptParser {

    private val currencyPrefix = Regex("""(?i)\b(rp\.?|idr)""")
    private val trailingDecimals = Regex("""[.,]\d{2}$""")
    private val amountPattern = Regex("""(?i)(?:(?:rp\.?|idr)\s*)?\d[\d.,]*\d|\d""")

    private val totalKeywords = listOf(
        "total belanja", "grand total", "total bayar", "total pembayaran", "total tagihan", "jumlah bayar", "total",
    )
    private val ignoredTotalWords = listOf("item", "disc", "diskon", "qty", "hemat", "poin")
    private val fallbackKeywords = listOf("nominal", "jumlah")
    private val numericDate = Regex("""\b(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})\b""")
    private val numericDateShortYear = Regex("""\b(\d{1,2})[-/.](\d{1,2})[-/.](\d{2})\b""")
    private val namedDate = Regex("""\b(\d{1,2})[\s-]+([A-Za-z]{3,9})[\s-]+(\d{4})\b""")
    private val dateLabels = listOf("tgl", "tanggal", "date", "waktu")
    private val monthsByPrefix = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "mei" to 5, "may" to 5,
        "jun" to 6, "jul" to 7, "agu" to 8, "ags" to 8, "aug" to 8, "sep" to 9,
        "okt" to 10, "oct" to 10, "nov" to 11, "des" to 12, "dec" to 12,
    )
    private val merchantLabels = listOf("penerima", "merchant", "nama toko", "tujuan", "kepada")
    private val knownMerchants = listOf(
        "Indomaret", "Alfamart", "Alfamidi", "Lawson", "Circle K", "FamilyMart", "Superindo",
        "Hypermart", "Transmart", "Lotte Mart", "Starbucks", "Kopi Kenangan", "Janji Jiwa",
        "Fore Coffee", "Chatime", "McDonald's", "KFC", "Burger King", "Pizza Hut", "Gojek", "Grab",
        "Shopee", "Tokopedia", "Guardian", "Watsons", "Kimia Farma",
    ).map { name -> name to Regex("""\b${Regex.escape(name)}\b""", RegexOption.IGNORE_CASE) }

    private val nonMerchantPrefixes = listOf("transfer", "pembayaran", "bukti", "struk", "npwp", "bon", "tgl", "tanggal", "kasir")

    fun parse(lines: List<String>): ParsedReceipt = ParsedReceipt(
        amount = parseAmount(lines),
        merchant = parseMerchant(lines),
        date = parseDate(lines),
    )

    fun parseRupiah(text: String): Long? {
        val cleaned = text.replace(currencyPrefix, "").filterNot { it.isWhitespace() }
        if (cleaned.isEmpty() || cleaned.any { !it.isDigit() && it != '.' && it != ',' }) return null
        val withoutDecimals = cleaned.replace(trailingDecimals, "")
        return withoutDecimals.filter { it.isDigit() }.toLongOrNull()
    }

    fun parseAmount(lines: List<String>): Long? {
        for (keyword in totalKeywords) {
            lines.firstNotNullOfOrNull { line ->
                val lower = line.lowercase()
                if (keyword in lower && ignoredTotalWords.none { it in lower }) lastAmountIn(line) else null
            }?.let { return it }
        }
        for (keyword in fallbackKeywords) {
            lines.firstNotNullOfOrNull { line ->
                if (keyword in line.lowercase()) lastAmountIn(line) else null
            }?.let { return it }
        }
        return null
    }

    fun parseDate(lines: List<String>): LocalDate? {
        val labelled = lines.filter { line -> dateLabels.any { it in line.lowercase() } }
        return (labelled + lines).firstNotNullOfOrNull { findDate(it) }
    }

    fun parseMerchant(lines: List<String>): String? {
        lines.firstNotNullOfOrNull { line ->
            val label = merchantLabels.firstOrNull { line.lowercase().startsWith(it) } ?: return@firstNotNullOfOrNull null
            line.drop(label.length).trim().removePrefix(":").trim().ifEmpty { null }
        }?.let { return it }

        lines.firstNotNullOfOrNull { line ->
            knownMerchants.firstOrNull { (_, pattern) -> pattern.containsMatchIn(line) }?.first
        }?.let { return it }

        return lines.firstOrNull { looksLikeName(it) }?.trim()
    }

    private fun lastAmountIn(line: String): Long? =
        amountPattern.findAll(line).mapNotNull { parseRupiah(it.value) }.lastOrNull()

    private fun findDate(line: String): LocalDate? {
        numericDate.find(line)?.let { match ->
            val (day, month, year) = match.destructured
            return dateOrNull(year.toInt(), month.toInt(), day.toInt())
        }
        namedDate.find(line)?.let { match ->
            val (day, monthName, year) = match.destructured
            val month = monthsByPrefix[monthName.take(3).lowercase()] ?: return null
            return dateOrNull(year.toInt(), month, day.toInt())
        }
        numericDateShortYear.find(line)?.let { match ->
            val (day, month, year) = match.destructured
            return dateOrNull(2000 + year.toInt(), month.toInt(), day.toInt())
        }
        return null
    }

    private fun dateOrNull(year: Int, month: Int, day: Int): LocalDate? =
        runCatching { LocalDate(year, month, day) }.getOrNull()

    private fun looksLikeName(line: String): Boolean {
        val letters = line.count { it.isLetter() }
        val visible = line.count { !it.isWhitespace() }
        val lower = line.trim().lowercase()
        return letters >= 3 && letters * 2 >= visible && nonMerchantPrefixes.none { lower.startsWith(it) }
    }
}
