package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.Project90Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.regex.Pattern

data class ParsedBankTransaction(
    val bankName: String,
    val amount: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val description: String,
    val isSalary: Boolean
)

/**
 * PHẦN 2: Nâng cấp NotificationListenerService đọc tất cả biến động số dư
 * từ các Ví điện tử (MoMo, Cake, ZaloPay, Viettel Money) và Ngân hàng (Vietcombank, MB Bank, TNEX, TPBank, Techcombank, BIDV, VPBank, ACB...).
 * Tự động cộng/trừ số dư vào đúng AccountWalletEntity tương ứng và ghi lịch sử FinanceTransactionEntity.
 */
class BankNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val _parsedNotificationEvents = MutableSharedFlow<ParsedBankTransaction>(extraBufferCapacity = 20)
        val parsedNotificationEvents: SharedFlow<ParsedBankTransaction> = _parsedNotificationEvents.asSharedFlow()

        @Volatile
        private var lastProcessedSignature: String = ""

        @Volatile
        private var lastProcessedTimestampMs: Long = 0L

        // Mở rộng danh sách package của các Ví điện tử & Ngân hàng phổ biến tại Việt Nam
        val SUPPORTED_BANK_PACKAGES = setOf(
            "com.mservice.momotransfer",    // Ví MoMo
            "vn.momo.platform",             // MoMo Platform
            "vn.cake",                      // Ngân hàng số Cake by VPBank
            "com.vpbank.cake",              // Cake by VPBank
            "vn.com.vng.zalopay",           // ZaloPay
            "com.bplus.vtpay",              // Viettel Money
            "com.vcb",                      // Vietcombank
            "com.mbmobile",                 // MB Bank
            "com.msb.tnex",                 // TNEX
            "com.tpb.mb.gprsandroid",       // TPBank
            "com.techcombank.bb.app",       // Techcombank
            "vn.com.techcombank.bb.app",    // Techcombank OneMount
            "com.vnpay.vpbankonline",       // VPBank NEO
            "com.vnpay.bidv",               // BIDV SmartBanking
            "com.vietinbank.ipay",          // VietinBank iPay
            "mobile.acb.com.vn",            // ACB One
            "src.com.sacombank.ewallet",    // Sacombank Pay
            "com.vib.myvib2",               // MyVIB
            "com.vnpay.Agribank3g",         // Agribank Plus
            "com.vnpay.hdbank"              // HDBank
        )

        // Ưu tiên 1: Bắt trực tiếp con số có dấu +/- kèm đơn vị tiền tệ hoặc sau từ khóa GD/Biến động/Số tiền
        private val EXPLICIT_SIGNED_AMOUNT_REGEX = Pattern.compile(
            "([+-])\\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,12})\\s*(?:vnd|vnđ|đ|d)?",
            Pattern.CASE_INSENSITIVE
        )

        // Ưu tiên 2: Bắt con số đứng trước đơn vị tiền tệ (VND, VNĐ, đ, d) cho các thông báo dạng "vừa giảm 150.000đ" hoặc "nhận được 500,000 VND"
        private val CURRENCY_AMOUNT_REGEX = Pattern.compile(
            "([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,12})\\s*(?:vnd|vnđ|đ|d)\\b",
            Pattern.CASE_INSENSITIVE
        )

        // Ưu tiên 3: Bắt con số định dạng hàng nghìn (VD: 25.000.000 hoặc 150,000)
        private val GENERAL_AMOUNT_REGEX = Pattern.compile(
            "([+-]?\\s*[0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,12})\\s*(?:vnd|vnđ|đ|d)?",
            Pattern.CASE_INSENSITIVE
        )

        private fun normalizeAscii(input: String): String {
            return try {
                val temp = Normalizer.normalize(input, Normalizer.Form.NFD)
                temp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                    .replace('đ', 'd')
                    .replace('Đ', 'D')
                    .lowercase()
            } catch (_: Exception) {
                input.lowercase()
            }
        }

        fun resolveWalletName(title: String, body: String, packageName: String): String {
            val pkg = packageName.lowercase()
            val combined = "$title $body".lowercase()
            val ascii = normalizeAscii(combined)

            return when {
                pkg.contains("momo") || pkg.contains("mservice") || ascii.contains("momo") -> "MoMo"
                pkg.contains("cake") || ascii.contains("cake") -> "Cake"
                pkg.contains("zalopay") || ascii.contains("zalopay") -> "ZaloPay"
                pkg.contains("vtpay") || ascii.contains("viettel money") -> "Viettel Money"
                pkg.contains("tnex") || ascii.contains("tnex") -> "TNEX"
                pkg.contains("mbmobile") || ascii.contains("mb bank") || ascii.contains("mbbank") -> "MB Bank"
                pkg.contains("vcb") || ascii.contains("vietcombank") || ascii.contains("vcb") -> "Vietcombank"
                pkg.contains("tpb") || ascii.contains("tpbank") -> "TPBank"
                pkg.contains("techcombank") || ascii.contains("tcb") -> "Techcombank"
                pkg.contains("vpbank") || ascii.contains("vpbank") -> "VPBank"
                pkg.contains("bidv") || ascii.contains("bidv") -> "BIDV"
                pkg.contains("vietinbank") || ascii.contains("vietinbank") -> "VietinBank"
                pkg.contains("acb") || ascii.contains("acb") -> "ACB"
                pkg.contains("sacombank") -> "Sacombank"
                pkg.contains("vib") -> "VIB"
                pkg.contains("agribank") -> "Agribank"
                else -> if (packageName.isNotEmpty()) "Ngân Hàng" else "TNEX / MB Bank"
            }
        }

        fun parseBankMessage(title: String, body: String, packageName: String = ""): ParsedBankTransaction? {
            return try {
                val combinedRaw = "$title $body".trim()
                if (combinedRaw.isBlank()) return null

                val combinedLower = combinedRaw.lowercase()
                val ascii = normalizeAscii(combinedRaw)
                val walletName = resolveWalletName(title, body, packageName)

                // 1. Thử tìm con số có dấu +/- rõ ràng (VD: "+250.000đ", "- 55,000 VND")
                var detectedSign: String? = null
                var extractedAmount = 0L

                val signedMatcher = EXPLICIT_SIGNED_AMOUNT_REGEX.matcher(body)
                if (signedMatcher.find()) {
                    detectedSign = signedMatcher.group(1)?.trim()
                    val rawDigits = signedMatcher.group(2)?.replace(".", "")?.replace(",", "")?.trim()
                    extractedAmount = rawDigits?.toLongOrNull() ?: 0L
                }

                if (extractedAmount <= 0L) {
                    val titleSignedMatcher = EXPLICIT_SIGNED_AMOUNT_REGEX.matcher(title)
                    if (titleSignedMatcher.find()) {
                        detectedSign = titleSignedMatcher.group(1)?.trim()
                        val rawDigits = titleSignedMatcher.group(2)?.replace(".", "")?.replace(",", "")?.trim()
                        extractedAmount = rawDigits?.toLongOrNull() ?: 0L
                    }
                }

                // 2. Xác định chiều biến động Thu (+) hay Chi (-)
                val hasIncomeKeyword = detectedSign == "+" ||
                        combinedLower.contains("+") ||
                        ascii.contains("nhan duoc") ||
                        ascii.contains("nhan tien") ||
                        ascii.contains("vua tang") ||
                        ascii.contains("tang +") ||
                        ascii.contains("cong tien") ||
                        ascii.contains("hoan tien") ||
                        ascii.contains("credit")

                val hasExpenseKeyword = detectedSign == "-" ||
                        combinedLower.contains("-") ||
                        ascii.contains("thanh toan") ||
                        ascii.contains("chi tieu") ||
                        ascii.contains("chuyen tien") ||
                        ascii.contains("vua giam") ||
                        ascii.contains("tru tien") ||
                        ascii.contains("rut tien") ||
                        ascii.contains("mua hang") ||
                        ascii.contains("debit")

                if (!hasIncomeKeyword && !hasExpenseKeyword) {
                    return null
                }

                val type = when {
                    detectedSign == "+" -> "INCOME"
                    detectedSign == "-" -> "EXPENSE"
                    hasIncomeKeyword && !hasExpenseKeyword -> "INCOME"
                    hasExpenseKeyword && !hasIncomeKeyword -> "EXPENSE"
                    else -> if (combinedLower.indexOf('+') in 0 until combinedLower.indexOf('-').let { if (it < 0) Int.MAX_VALUE else it }) {
                        "INCOME"
                    } else {
                        "EXPENSE"
                    }
                }

                // 3. Nếu chưa bắt được từ EXPLICIT_SIGNED_AMOUNT_REGEX, dùng CURRENCY_AMOUNT_REGEX hoặc GENERAL_AMOUNT_REGEX
                if (extractedAmount <= 0L) {
                    val currencyMatcher = CURRENCY_AMOUNT_REGEX.matcher(body)
                    if (currencyMatcher.find()) {
                        val rawNum = currencyMatcher.group(1)?.replace(".", "")?.replace(",", "")?.trim()
                        extractedAmount = rawNum?.toLongOrNull() ?: 0L
                    }
                }

                if (extractedAmount <= 0L) {
                    val generalMatcher = GENERAL_AMOUNT_REGEX.matcher(body)
                    if (generalMatcher.find()) {
                        val rawNum = generalMatcher.group(1)
                            ?.replace(".", "")
                            ?.replace(",", "")
                            ?.replace("+", "")
                            ?.replace("-", "")
                            ?.trim()
                        extractedAmount = rawNum?.toLongOrNull() ?: 0L
                    }
                }

                if (extractedAmount <= 0L) {
                    val titleMatcher = GENERAL_AMOUNT_REGEX.matcher(title)
                    if (titleMatcher.find()) {
                        val rawNum = titleMatcher.group(1)
                            ?.replace(".", "")
                            ?.replace(",", "")
                            ?.replace("+", "")
                            ?.replace("-", "")
                            ?.trim()
                        extractedAmount = rawNum?.toLongOrNull() ?: 0L
                    }
                }

                if (extractedAmount <= 0L) return null

                val isSalary = ascii.contains("luong") || ascii.contains("salary") || ascii.contains("thuong")
                val category = when {
                    isSalary -> "Lương"
                    ascii.contains("an uong") || ascii.contains("food") || ascii.contains("cafe") ||
                            ascii.contains("coffee") || ascii.contains("grabfood") || ascii.contains("shopeefood") -> "Ăn uống"
                    ascii.contains("gym") || ascii.contains("the thao") || ascii.contains("bong da") -> "Thể thao & Sức khỏe"
                    ascii.contains("xang") || ascii.contains("grab") || ascii.contains("be ") || ascii.contains("xanh sm") -> "Di chuyển"
                    ascii.contains("dien") || ascii.contains("nuoc") || ascii.contains("internet") || ascii.contains("viettel") -> "Hóa đơn & Sinh hoạt"
                    ascii.contains("tra no") || ascii.contains("khoan vay") || ascii.contains("fe credit") || ascii.contains("home credit") -> "Trả nợ khoản vay"
                    else -> if (type == "INCOME") "Thu nhập ($walletName)" else "Chi tiêu ($walletName)"
                }

                ParsedBankTransaction(
                    bankName = walletName,
                    amount = extractedAmount,
                    type = type,
                    category = category,
                    description = body.ifBlank { title }.take(180),
                    isSalary = isSalary
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (sbn == null) return
            val packageName = sbn.packageName ?: ""
            // Bỏ qua thông báo từ chính app Project 90
            if (packageName == applicationContext.packageName) return

            val extras = sbn.notification?.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

            val fullBody = when {
                bigText.isNotBlank() && bigText.length >= text.length -> bigText
                subText.isNotBlank() -> "$text $subText"
                else -> text
            }

            // Kiểm tra xem có phải thông báo từ app Ngân hàng/Ví hoặc có chứa nội dung biến động số dư không
            val asciiCheck = normalizeAscii("$title $fullBody")
            val isSupportedPackage = packageName in SUPPORTED_BANK_PACKAGES ||
                    packageName.contains("momo", ignoreCase = true) ||
                    packageName.contains("cake", ignoreCase = true) ||
                    packageName.contains("bank", ignoreCase = true) ||
                    packageName.contains("pay", ignoreCase = true) ||
                    packageName.contains("vcb", ignoreCase = true) ||
                    packageName.contains("tnex", ignoreCase = true) ||
                    packageName.contains("mbmobile", ignoreCase = true)

            val hasBalanceKeywords = asciiCheck.contains("so du") ||
                    asciiCheck.contains("bien dong") ||
                    asciiCheck.contains("tk ") ||
                    asciiCheck.contains("gd:") ||
                    asciiCheck.contains("thanh toan") ||
                    asciiCheck.contains("nhan tien") ||
                    asciiCheck.contains("chuyen tien") ||
                    asciiCheck.contains("momo") ||
                    asciiCheck.contains("cake")

            if (!isSupportedPackage && !hasBalanceKeywords) return

            val parsed = parseBankMessage(title, fullBody, packageName) ?: return

            // Chống trùng lặp khi hệ thống Android gửi lại Group Summary Notification trong 2.5 giây
            val now = System.currentTimeMillis()
            val signature = "${parsed.bankName}|${parsed.type}|${parsed.amount}|${parsed.description.take(40)}"
            if (signature == lastProcessedSignature && (now - lastProcessedTimestampMs) < 2500L) {
                return
            }
            lastProcessedSignature = signature
            lastProcessedTimestampMs = now

            _parsedNotificationEvents.tryEmit(parsed)

            serviceScope.launch {
                try {
                    val repo = (application as? Project90Application)?.repository
                    repo?.addFinancialTransaction(
                        amount = parsed.amount,
                        type = parsed.type,
                        category = parsed.category,
                        bankSource = parsed.bankName,
                        description = parsed.description,
                        isSalary = parsed.isSalary
                    )
                    HabitNotificationHelper.postOrUpdateNotification(applicationContext)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Đảm bảo không bao giờ crash NotificationListenerService trên máy thật
        }
    }
}
