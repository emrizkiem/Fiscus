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
        "total belanja", "grand total", "total bayar", "total pembayaran", "total tagihan", "jumlah bayar", "jml bayar", "total",
    )
    private val ignoredTotalWords = listOf("item", "disc", "diskon", "qty", "hemat", "poin")
    private val fallbackKeywords = listOf("nominal", "jumlah", "jml")
    private val feeKeywords = listOf("biaya admin", "admin bank", "biaya transfer", "biaya layanan")
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
    private val knownMerchants = brandPatterns(
        "Indomaret" to listOf("indomaret", "klikindomaret"),
        "Alfamart" to listOf("alfamart", "alfagift"),
        "Alfamidi" to listOf("alfamidi"),
        "Lawson" to listOf("lawson"),
        "Circle K" to listOf("circle k"),
        "FamilyMart" to listOf("familymart"),
        "Superindo" to listOf("superindo"),
        "Hypermart" to listOf("hypermart"),
        "Transmart" to listOf("transmart"),
        "Lotte Mart" to listOf("lotte mart"),
        "Starbucks" to listOf("starbucks"),
        "Kopi Kenangan" to listOf("kopi kenangan"),
        "Janji Jiwa" to listOf("janji jiwa"),
        "Fore Coffee" to listOf("fore coffee"),
        "Chatime" to listOf("chatime"),
        "McDonald's" to listOf("mcdonald's", "mcdonalds"),
        "KFC" to listOf("kfc"),
        "Burger King" to listOf("burger king"),
        "Pizza Hut" to listOf("pizza hut"),
        "Gojek" to listOf("gojek"),
        "Grab" to listOf("grab"),
        "Shopee" to listOf("shopee"),
        "Tokopedia" to listOf("tokopedia"),
        "Guardian" to listOf("guardian"),
        "Watsons" to listOf("watsons"),
        "Kimia Farma" to listOf("kimia farma"),
    )

    private val knownIssuers = brandPatterns(
        "BCA" to listOf("bca", "klikbca"),
        "Bank Mandiri" to listOf("bank mandiri", "livin"),
        "BRI" to listOf("bri", "brimo"),
        "BNI" to listOf("bni"),
        "Bank Sinarmas" to listOf("sinarmas"),
        "CIMB Niaga" to listOf("cimb"),
        "BSI" to listOf("bsi", "bank syariah indonesia"),
        "GoPay" to listOf("gopay", "go-pay"),
        "OVO" to listOf("ovo"),
        "ShopeePay" to listOf("shopeepay"),
    )
    private const val HEADER_LINE_COUNT = 3

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
            }?.let { base -> return base + adminFee(lines) }
        }
        return null
    }

    private fun adminFee(lines: List<String>): Long =
        lines.firstNotNullOfOrNull { line ->
            if (feeKeywords.any { it in line.lowercase() }) lastAmountIn(line) else null
        } ?: 0L

    fun parseDate(lines: List<String>): LocalDate? {
        val labelled = lines.filter { line -> dateLabels.any { it in line.lowercase() } }
        return (labelled + lines).firstNotNullOfOrNull { findDate(it) }
    }

    fun parseMerchant(lines: List<String>): String? {
        lines.firstNotNullOfOrNull { line ->
            val label = merchantLabels.firstOrNull { line.lowercase().startsWith(it) } ?: return@firstNotNullOfOrNull null
            line.drop(label.length).trim().removePrefix(":").trim().ifEmpty { null }
        }?.let { return it }

        findBrand(lines, knownMerchants)?.let { return it }
        findBrand(lines.take(HEADER_LINE_COUNT), knownIssuers)?.let { return it }

        return lines.firstOrNull { looksLikeName(it) }?.trim()
    }

    private fun findBrand(lines: List<String>, brands: List<Pair<String, List<Regex>>>): String? =
        lines.firstNotNullOfOrNull { line ->
            brands.firstOrNull { (_, patterns) -> patterns.any { it.containsMatchIn(line) } }?.first
        }

    private fun brandPatterns(vararg brands: Pair<String, List<String>>): List<Pair<String, List<Regex>>> =
        brands.map { (name, aliases) ->
            name to aliases.map { alias -> Regex("""\b${Regex.escape(alias)}\b""", RegexOption.IGNORE_CASE) }
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
