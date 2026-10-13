package com.example.fiscus.data.parser

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReceiptParserTest {
    private val alfamartAndroid = listOf(
        "Alfamart",
        "ALFAMART MARGASARI / 081294677446",
        "MARGASARI (MGA)",
        "ALFA TOWER LT.12, ALAM",
        "SUTERA,TANGERANG",
        "NPWP: 01.336.238.9-054.000",
        "Bon 2G54-255-1809Y9T3 Kasir :HELMA",
        "GURIBEE RL 65G 9,100 9,100",
        "WLS CNT SMI08ML 1,000 22,000",
        "ALFA MSS MLK90G 11\\,500 1,500",
        "Disc. -2,000",
        "Total Item 11 133,000",
        "Total Disc. 5,400",
        "Total Belanja 127,600",
        "QRIS SOUNDBOX 127,600",
        "Kembalian",
        "PPN DPP: 117,207 PPN: 12,892",
        "Tgl. 18-09-2026 10:58:22 V.2026.7.2",
        "MEMBER: RIZKI ****** *",
        "1606 poin Anda akan expired pada",
        "30-Sep-2026",
    )

    private val alfamartIos = listOf(
        "Alfamart",
        "ALFAMART MARGASARI /",
        "081294677446",
        "MARGASARI [MGAI]",
        "NPWP : 01.336.238.9-054.000",
        "Bon 2G54-255-1809Y9T3 Kasir : HELMA",
        "IM PUTIH 535G 18,000 18,000",
        "Disc. - 2,800",
        "Total Item 11 133,000",
        "Total Disc. 5,400",
        "Total Belanja 127,600",
        "QRIS SOUNDBOX 127,600",
        "Kembalian",
        "PPN DPP: 117,207 PPN: 12,892",
        "Tgl. 18-09-2026 10:58:22 V.2026.7.2",
        "30-Sep-2026",
    )

    @Test
    fun parseRupiahReadsCommaAndDotThousandSeparators() {
        assertEquals(127_600, ReceiptParser.parseRupiah("127,600"))
        assertEquals(127_600, ReceiptParser.parseRupiah("127.600"))
        assertEquals(1_842_500, ReceiptParser.parseRupiah("1.842.500"))
        assertEquals(1_842_500, ReceiptParser.parseRupiah("1,842,500"))
        assertEquals(500, ReceiptParser.parseRupiah("500"))
    }

    @Test
    fun parseRupiahIgnoresRpPrefixAndSpaces() {
        assertEquals(62_500, ReceiptParser.parseRupiah("Rp 62.500"))
        assertEquals(62_500, ReceiptParser.parseRupiah("Rp62.500"))
        assertEquals(62_500, ReceiptParser.parseRupiah("Rp. 62.500"))
        assertEquals(62_500, ReceiptParser.parseRupiah("IDR 62.500"))
    }

    @Test
    fun parseRupiahDropsTwoDigitDecimals() {
        assertEquals(62_500, ReceiptParser.parseRupiah("62.500,00"))
        assertEquals(62_500, ReceiptParser.parseRupiah("62,500.00"))
        assertEquals(62_500, ReceiptParser.parseRupiah("Rp 62.500,00"))
    }

    @Test
    fun parseRupiahReturnsNullForNonNumbers() {
        assertNull(ReceiptParser.parseRupiah(""))
        assertNull(ReceiptParser.parseRupiah("Kembalian"))
    }

    @Test
    fun amountFromAlfamartAndroidIsTotalBelanja() {
        assertEquals(127_600, ReceiptParser.parseAmount(alfamartAndroid))
    }

    @Test
    fun amountFromAlfamartIosIsTotalBelanja() {
        assertEquals(127_600, ReceiptParser.parseAmount(alfamartIos))
    }

    @Test
    fun amountIgnoresTotalItemAndTotalDiscount() {
        val lines = listOf("Total Item 3 50,000", "Total Disc. 5,000", "Total 45,000")

        assertEquals(45_000, ReceiptParser.parseAmount(lines))
    }

    @Test
    fun amountKeywordsAreCaseInsensitive() {
        assertEquals(45_500, ReceiptParser.parseAmount(listOf("TOTAL BAYAR : Rp 45.500")))
        assertEquals(85_000, ReceiptParser.parseAmount(listOf("Grand Total Rp 85.000")))
    }

    @Test
    fun amountFromBankTransferUsesTotalIncludingFee() {
        val lines = listOf(
            "Transfer Berhasil",
            "Penerima BUDI SANTOSO",
            "Nominal Rp 250.000",
            "Biaya Admin Rp 6.500",
            "Total Rp 256.500",
        )

        assertEquals(256_500, ReceiptParser.parseAmount(lines))
    }

    @Test
    fun amountFallsBackToNominalWhenThereIsNoTotal() {
        val lines = listOf("Transfer Berhasil", "Nominal Rp 50.000", "Biaya Admin Rp 0")

        assertEquals(50_000, ReceiptParser.parseAmount(lines))
    }

    @Test
    fun amountIsNullWhenNothingLooksLikeATotal() {
        val lines = listOf("Alfamart", "NPWP: 01.336.238.9-054.000", "Kasir : HELMA")

        assertNull(ReceiptParser.parseAmount(lines))
    }

    @Test
    fun dateFromAlfamartPrefersTglLineOverExpiryDate() {
        assertEquals(LocalDate(2026, 9, 18), ReceiptParser.parseDate(alfamartAndroid))
        assertEquals(LocalDate(2026, 9, 18), ReceiptParser.parseDate(alfamartIos))
    }

    @Test
    fun dateReadsNumericFormats() {
        assertEquals(LocalDate(2026, 9, 18), ReceiptParser.parseDate(listOf("Tanggal 18/09/2026")))
        assertEquals(LocalDate(2026, 9, 18), ReceiptParser.parseDate(listOf("18-09-2026 10:58")))
        assertEquals(LocalDate(2026, 3, 5), ReceiptParser.parseDate(listOf("Tgl 5/3/2026")))
    }

    @Test
    fun dateReadsIndonesianAndEnglishMonthNames() {
        assertEquals(LocalDate(2026, 9, 18), ReceiptParser.parseDate(listOf("18 Sep 2026, 10:58 WIB")))
        assertEquals(LocalDate(2026, 8, 17), ReceiptParser.parseDate(listOf("17 Agustus 2026")))
        assertEquals(LocalDate(2026, 10, 1), ReceiptParser.parseDate(listOf("Waktu 01 Okt 2026")))
        assertEquals(LocalDate(2026, 5, 2), ReceiptParser.parseDate(listOf("2 May 2026")))
        assertEquals(LocalDate(2026, 12, 25), ReceiptParser.parseDate(listOf("25-Des-2026")))
    }

    @Test
    fun dateIsNullForInvalidOrMissingDates() {
        assertNull(ReceiptParser.parseDate(listOf("Tgl. 32-13-2026")))
        assertNull(ReceiptParser.parseDate(listOf("Total Belanja 127,600")))
    }

    @Test
    fun merchantFromAlfamartIsFirstLine() {
        assertEquals("Alfamart", ReceiptParser.parseMerchant(alfamartAndroid))
        assertEquals("Alfamart", ReceiptParser.parseMerchant(alfamartIos))
    }

    @Test
    fun merchantPrefersLabelledRecipient() {
        val lines = listOf("Transfer Berhasil", "Penerima BUDI SANTOSO", "Nominal Rp 50.000")

        assertEquals("BUDI SANTOSO", ReceiptParser.parseMerchant(lines))
    }

    @Test
    fun merchantSkipsLinesWithoutRealWords() {
        val lines = listOf("081294677446", "*****", "Indomaret", "Total 25.000")

        assertEquals("Indomaret", ReceiptParser.parseMerchant(lines))
    }

    @Test
    fun merchantIsNullForEmptyInput() {
        assertNull(ReceiptParser.parseMerchant(emptyList()))
    }

    @Test
    fun parseCombinesAllFields() {
        assertEquals(
            ParsedReceipt(amount = 127_600, merchant = "Alfamart", date = LocalDate(2026, 9, 18)),
            ReceiptParser.parse(alfamartIos),
        )
    }

    // ---------- struk Indomaret (tahun 2 digit, logo salah baca) ----------

    private val indomaret = listOf(
        "NI MENARA Indbmarel)",
        "INDOMARET BOULEVARD",
        "PANTAI INDAH KAPUK",
        "JAKARTA UTARA",
        "NPWP 001337.9946-092.000",
        "PAHLAWAN RAYA",
        "JL.VETERAN REMPOA RAYA BINTARO KEL.BINTARO",
        "KEC. PESANGGRAHAN, KOTA JAKSEL, 12330",
        "02.03.26-17:35/4.1.5/TBW4-1437/ASNIR/02",
        "PPSODENT FF PLAQ 1 18900 18,900",
        "TOTAL BELANJA : 18,900",
        "TUNAI : 18,900",
        ": DPP: 15,230 PPN: 1,873",
        "HARGA JUAL : 17,027",
        "LAYANAN KONSUMEN",
        "SMS/WA 0811.1600.280 TELP 1500208",
        "KONTAK@INDOMARET.CO.ID",
    )

    @Test
    fun indomaretReceiptIsParsedCorrectly() {
        assertEquals(
            ParsedReceipt(amount = 18_900, merchant = "Indomaret", date = LocalDate(2026, 3, 2)),
            ReceiptParser.parse(indomaret),
        )
    }

    @Test
    fun dateReadsTwoDigitYear() {
        assertEquals(LocalDate(2026, 3, 2), ReceiptParser.parseDate(listOf("02.03.26-17:35/4.1.5/TBW4-1437")))
        assertEquals(LocalDate(2026, 3, 5), ReceiptParser.parseDate(listOf("Tgl 05/03/26 10:58")))
        assertEquals(LocalDate(2026, 12, 31), ReceiptParser.parseDate(listOf("31-12-26")))
    }

    @Test
    fun fourDigitYearIsPreferredOverTwoDigitYear() {
        assertEquals(LocalDate(2026, 9, 18), ReceiptParser.parseDate(listOf("Tgl. 18-09-2026 10:58:22")))
    }

    @Test
    fun npwpAndPhoneNumbersAreNotMistakenForDates() {
        val lines = listOf("NPWP 001337.9946-092.000", "SMS/WA 0811.1600.280 TELP 1500208", "4.1.5/TBW4")

        assertNull(ReceiptParser.parseDate(lines))
    }

    @Test
    fun merchantUsesKnownBrandWhenLogoIsMisread() {
        assertEquals("Indomaret", ReceiptParser.parseMerchant(indomaret))
    }

    @Test
    fun knownBrandIsMatchedAsWholeWordOnly() {
        // "fore" di dalam "before" bukan merchant Fore
        val lines = listOf("Warung Bu Sri", "Bayar before 10:00")

        assertEquals("Warung Bu Sri", ReceiptParser.parseMerchant(lines))
    }

    @Test
    fun labelledRecipientStillWinsOverKnownBrand() {
        val lines = listOf("Transfer Berhasil", "Penerima BUDI SANTOSO", "Bayar via Alfamart")

        assertEquals("BUDI SANTOSO", ReceiptParser.parseMerchant(lines))
    }
}
