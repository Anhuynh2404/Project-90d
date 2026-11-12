package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AccountWalletEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FinanceTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.EcoBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.Project90ViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private enum class FinanceSubTab(val title: String) {
    MONEY("Thu Chi & Ví"),
    DEBT("Quản Lý Nợ"),
    SAVINGS("Quỹ Tiết Kiệm")
}

@Composable
fun FinanceScreen(
    viewModel: Project90ViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val totalSavings by viewModel.totalSavings.collectAsStateWithLifecycle()
    val totalGoalsSaved by viewModel.totalGoalsSaved.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(FinanceSubTab.MONEY) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddWalletDialog by remember { mutableStateOf(false) }
    var selectedWalletForEdit by remember { mutableStateOf<AccountWalletEntity?>(null) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<FinanceTransactionEntity?>(null) }

    var isWeeklyChart by remember { mutableStateOf(true) }

    val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoBackground)
                .testTag("finance_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header & Action Button
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "MÔ-ĐUN TÀI CHÍNH 2.0",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            ),
                            color = ForestGreen
                        )
                        Text(
                            text = "Quản lý Ví MoMo/Cake • Thu Chi • Khoản Vay • Quỹ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            when (activeSubTab) {
                                FinanceSubTab.MONEY -> showAddTxDialog = true
                                FinanceSubTab.DEBT -> showAddDebtDialog = true
                                FinanceSubTab.SAVINGS -> showAddGoalDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                        modifier = Modifier.testTag("finance_primary_add_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (activeSubTab) {
                                FinanceSubTab.MONEY -> "Ghi Thu/Chi"
                                FinanceSubTab.DEBT -> "Thêm Khoản Vay"
                                FinanceSubTab.SAVINGS -> "Tạo Quỹ Mới"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 2. 3-Feature Segmented Tab Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = ForestGreen.copy(alpha = 0.12f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardWhite)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FinanceSubTab.entries.forEach { tab ->
                        val isSelected = activeSubTab == tab
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(ForestGreen, LeafGreen))
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { activeSubTab = tab }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // ==================== TAB 1: QUẢN LÝ TIỀN, VÍ/NGÂN HÀNG & BIỂU ĐỒ ====================
            if (activeSubTab == FinanceSubTab.MONEY) {
                // PHẦN 2: Quản lý Ví điện tử & Ngân hàng (MoMo, Cake, VCB, MB Bank, TNEX, TPBank...)
                item {
                    WalletManagementSectionCard(
                        wallets = wallets,
                        formatter = formatter,
                        onAddWalletClick = { showAddWalletDialog = true },
                        onWalletClick = { selectedWalletForEdit = it }
                    )
                }

                // Summary Cards: Thu Nhập, Chi Tiêu, Số Dư Khả Dụng
                item {
                    val safeIn = totalIncome ?: 0L
                    val safeOut = totalExpense ?: 0L
                    val safeSav = maxOf(totalSavings ?: 0L, totalGoalsSaved ?: 0L)
                    val netBalance = safeIn - safeOut - (totalSavings ?: 0L)

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SuccessGreen.copy(alpha = 0.12f))
                                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tổng Thu Nhập", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${formatter.format(safeIn)} đ",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SuccessGreen,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = DangerCrimson.copy(alpha = 0.12f))
                                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tổng Chi Tiêu", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${formatter.format(safeOut)} đ",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DangerCrimson,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MintLight),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ForestGreen.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("SỐ DƯ KHẢ DỤNG SAU TRÍCH QUỸ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                    Text(
                                        text = "${formatter.format(netBalance)} đ",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestGreenDark
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ĐÃ KHÓA TIẾT KIỆM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarmAmber)
                                    Text(
                                        text = "${formatter.format(safeSav)} đ",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Weekly / Monthly Statistical Chart Card
                item {
                    FinanceStatisticsChartCard(
                        transactions = transactions,
                        todayEpochDay = todayEpoch,
                        isWeekly = isWeeklyChart,
                        onTogglePeriod = { isWeeklyChart = it },
                        formatter = formatter
                    )
                }

                // Category Breakdown Card
                item {
                    CategoryBreakdownCard(
                        transactions = transactions,
                        formatter = formatter
                    )
                }

                // PHẦN 2: Bank & E-Wallet Notification Listener Automation Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MintLight)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tự Động Đọc Biến Động Số Dư Ví & Ngân Hàng",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "MoMo • Cake • Vietcombank • MB Bank • TNEX • TPBank • ZaloPay",
                                        fontSize = 11.sp,
                                        color = ForestGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Tự động cộng/trừ tiền vào đúng Ví hoặc Ngân hàng tương ứng",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    } catch (_: Exception) {}
                                }
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Cài đặt quyền đọc thông báo", tint = ForestGreen)
                            }
                        }
                    }
                }

                // Transaction List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LỊCH SỬ GIAO DỊCH CHI TIẾT (${transactions.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Text(
                            text = "Chạm vào giao dịch để xem chi tiết",
                            fontSize = 10.sp,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (transactions.isEmpty()) {
                    item {
                        EmptyFinanceStateCard(
                            title = "Chưa có giao dịch thu/chi nào",
                            subtitle = "Nhấn 'Ghi Thu/Chi' ở góc trên hoặc bật đọc thông báo MoMo/Cake/Ngân hàng để tự động ghi nhận."
                        )
                    }
                } else {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemCard(
                            tx = tx,
                            formatter = formatter,
                            onClick = { selectedTransactionForDetail = tx },
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }

            // ==================== TAB 2: PHẦN 3 - QUẢN LÝ KHOẢN VAY / NỢ (NGÂN HÀNG, CT TÀI CHÍNH, BẠN BÈ) ====================
            if (activeSubTab == FinanceSubTab.DEBT) {
                item {
                    val activeDebts = debts.filter { !it.isSettled }
                    val totalRemainingDebt = activeDebts.sumOf { it.remainingDebtAmount }
                    val totalMonthlyInstallment = activeDebts.sumOf {
                        if (it.monthlyInstallment > 0L) it.monthlyInstallment else it.remainingDebtAmount
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = DangerCrimson.copy(alpha = 0.14f))
                                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("TỔNG DƯ NỢ CÒN PHẢI TRẢ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DangerCrimson)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${formatter.format(totalRemainingDebt)} đ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DangerCrimson,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${activeDebts.size} khoản vay đang trả góp",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = WarmAmber.copy(alpha = 0.14f))
                                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("ĐÓNG HÀNG THÁNG (GỐC+LÃI)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarmAmber)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${formatter.format(totalMonthlyInstallment)} đ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForestGreenDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tự động tính số tháng còn lại",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                if (debts.isEmpty()) {
                    item {
                        EmptyFinanceStateCard(
                            title = "Chưa có khoản vay hoặc công nợ nào",
                            subtitle = "Hỗ trợ theo dõi Vay Ngân Hàng, Công Ty Tài Chính (FE Credit, Home Credit...) hoặc Bạn Bè với tiến độ số tháng đã đóng và đếm ngược ngày trả lãi hàng tháng."
                        )
                    }
                } else {
                    items(debts, key = { it.id }) { debt ->
                        val defaultWalletName = wallets.firstOrNull()?.name ?: "Vietcombank"
                        LoanDebtItemCard(
                            debt = debt,
                            formatter = formatter,
                            onPayNextMonth = {
                                viewModel.payLoanMonthlyInstallment(debt, defaultWalletName)
                            },
                            onToggleSettled = { viewModel.toggleDebtSettled(debt) },
                            onDelete = { viewModel.deleteDebt(debt.id) }
                        )
                    }
                }
            }

            // ==================== TAB 3: QUẢN LÝ TIẾT KIỆM (QUỸ MỤC TIÊU) ====================
            if (activeSubTab == FinanceSubTab.SAVINGS) {
                item {
                    val totalSavedAllGoals = savingsGoals.sumOf { it.currentAmount }
                    val totalTargetAllGoals = savingsGoals.sumOf { it.targetAmount }.coerceAtLeast(1L)
                    val overallGoalPercent = ((totalSavedAllGoals.toDouble() / totalTargetAllGoals.toDouble()) * 100).roundToInt().coerceIn(0, 100)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MintLight),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = ForestGreen.copy(alpha = 0.16f))
                            .border(1.dp, ForestGreen.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(ForestGreen)
                                    ) {
                                        Icon(Icons.Default.Savings, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "TỔNG TÀI SẢN QUỸ MỤC TIÊU",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                            color = ForestGreen,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Trích tiền từ thu nhập vào mục tiêu lớn",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ForestGreen)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$overallGoalPercent%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${formatter.format(totalSavedAllGoals)} đ",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = ForestGreenDark
                            )
                            Text(
                                text = "Trên tổng mục tiêu: ${formatter.format(totalTargetAllGoals)} đ",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                items(savingsGoals, key = { it.id }) { goal ->
                    SavingsGoalItemCard(
                        goal = goal,
                        formatter = formatter,
                        onDepositClick = { selectedGoalForDeposit = goal },
                        onDeleteClick = { viewModel.deleteSavingsGoal(goal.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // ==================== DIALOG 1: GHI THU / CHI MỚI (TÍCH HỢP CHỌN VÍ / NGÂN HÀNG) ====================
        if (showAddTxDialog) {
            var amountText by remember { mutableStateOf("") }
            var isIncome by remember { mutableStateOf(false) }
            var isSalary by remember { mutableStateOf(false) }
            val categories = if (isIncome) {
                listOf("Lương", "Thưởng / Freelance", "Đầu tư sinh lời", "Thu khác")
            } else {
                listOf("Ăn uống", "Tập gym & Sức khỏe", "Di chuyển", "Học tập & Sách", "Sinh hoạt", "Trả nợ khoản vay", "Khác")
            }
            var selectedCategory by remember(isIncome) { mutableStateOf(categories.first()) }
            val walletOptions = remember(wallets) {
                if (wallets.isNotEmpty()) wallets.map { it.name }
                else listOf("MoMo", "Cake", "Vietcombank", "MB Bank", "TNEX", "TPBank", "Tiền mặt")
            }
            var bankSource by remember(walletOptions) { mutableStateOf(walletOptions.first()) }
            var description by remember { mutableStateOf("") }

            FinanceModalDialog(
                onDismissRequest = { showAddTxDialog = false },
                title = {
                    Text("Ghi Chép Thu / Chi Mới", fontWeight = FontWeight.Black, color = ForestGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isIncome = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isIncome) DangerCrimson else MintLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Chi Tiêu (-)", color = if (!isIncome) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isIncome = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) SuccessGreen else MintLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Thu Nhập (+)", color = if (isIncome) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { raw ->
                                amountText = if (raw.trimStart().startsWith("-")) {
                                    ""
                                } else {
                                    raw.filter { ch -> ch.isDigit() }.take(15)
                                }
                            },
                            label = { Text("Số tiền (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tx_amount_input")
                        )

                        Text("Chọn Ví / Ngân hàng (Tự động trừ/cộng số dư):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            walletOptions.forEach { wName ->
                                FilterChip(
                                    selected = bankSource.equals(wName, ignoreCase = true),
                                    onClick = { bankSource = wName },
                                    label = { Text(wName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MarineAqua,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text("Chọn danh mục:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Ghi chú chi tiết") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isIncome) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MintLight, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tự động trích 10% vào Quỹ?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                    Text("Khóa ngay 10% vào Quỹ Tiết Kiệm", fontSize = 10.sp, color = WarmAmber)
                                }
                                Switch(
                                    checked = isSalary,
                                    onCheckedChange = { isSalary = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintLight)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            if (amount > 0L) {
                                viewModel.addTransaction(
                                    amount = amount,
                                    type = if (isIncome) "INCOME" else "EXPENSE",
                                    category = selectedCategory,
                                    bankSource = bankSource,
                                    description = description.ifBlank { "$selectedCategory ($bankSource)" },
                                    isSalary = isSalary
                                )
                                showAddTxDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.testTag("tx_save_button")
                    ) {
                        Text("Lưu Giao Dịch", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTxDialog = false }) {
                        Text("Hủy", color = TextSecondary)
                    }
                },
                containerColor = CardWhite
            )
        }

        // ==================== DIALOG 2: PHẦN 3 - GHI CHÉP KHOẢN VAY / NỢ MỚI (NGÂN HÀNG, CT TÀI CHÍNH, BẠN BÈ) ====================
        if (showAddDebtDialog) {
            var loanType by remember { mutableStateOf("BANK") } // "BANK", "FINANCE_COMPANY", "FRIEND"
            val lenderPresets = remember(loanType) {
                when (loanType) {
                    "BANK" -> listOf("Vietcombank", "VPBank", "TPBank", "MB Bank", "Techcombank", "BIDV", "VIB")
                    "FINANCE_COMPANY" -> listOf("FE Credit", "Home Credit", "HD Saison", "Shinhan Finance", "Mirae Asset", "MoMo Vay Nhanh")
                    else -> listOf("Bạn thân", "Anh/Chị/Em", "Đồng nghiệp", "Người quen")
                }
            }
            var personName by remember { mutableStateOf("") }
            var amountText by remember { mutableStateOf("") }
            var monthlyInstallmentText by remember { mutableStateOf("") }
            var totalMonthsText by remember { mutableStateOf("12") }
            var paidMonthsText by remember { mutableStateOf("0") }
            var dueDayText by remember { mutableStateOf("15") }
            var note by remember { mutableStateOf("") }

            // Tự động tính toán preview ngay khi người dùng nhập
            val parsedTotalLoan = amountText.toLongOrNull() ?: 0L
            val parsedTotalMonths = (totalMonthsText.toIntOrNull() ?: 1).coerceAtLeast(1)
            val parsedPaidMonths = (paidMonthsText.toIntOrNull() ?: 0).coerceIn(0, parsedTotalMonths)
            val remainingMonthsPreview = (parsedTotalMonths - parsedPaidMonths).coerceAtLeast(0)
            val parsedMonthly = monthlyInstallmentText.toLongOrNull()
                ?: if (parsedTotalLoan > 0L) (parsedTotalLoan / parsedTotalMonths.toLong()) else 0L
            val remainingMoneyPreview = (remainingMonthsPreview.toLong() * parsedMonthly).coerceAtLeast(0L)

            FinanceModalDialog(
                onDismissRequest = { showAddDebtDialog = false },
                title = {
                    Text("Ghi Chép Khoản Vay / Trả Góp Mới", fontWeight = FontWeight.Black, color = ForestGreen)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text("1. Phân loại nguồn vay:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "BANK" to "Ngân Hàng",
                                "FINANCE_COMPANY" to "CT Tài Chính",
                                "FRIEND" to "Bạn Bè"
                            ).forEach { (code, label) ->
                                val selected = loanType == code
                                Button(
                                    onClick = { loanType = code },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) ForestGreen else MintLight
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Text("2. Chọn nhanh nơi vay hoặc tự nhập:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            lenderPresets.forEach { preset ->
                                FilterChip(
                                    selected = personName == preset,
                                    onClick = { personName = preset },
                                    label = { Text(preset, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MarineAqua,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = personName,
                            onValueChange = { personName = it },
                            label = { Text("Nơi vay / Người cho vay") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debt_person_input")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { raw ->
                                    amountText = if (raw.trimStart().startsWith("-")) {
                                        ""
                                    } else {
                                        raw.filter { ch -> ch.isDigit() }.take(15)
                                    }
                                },
                                label = { Text("Tổng tiền vay (VND)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("debt_amount_input")
                            )

                            OutlinedTextField(
                                value = monthlyInstallmentText,
                                onValueChange = { raw ->
                                    monthlyInstallmentText = if (raw.trimStart().startsWith("-")) {
                                        ""
                                    } else {
                                        raw.filter { ch -> ch.isDigit() }.take(15)
                                    }
                                },
                                label = { Text("Trả/tháng (Gốc+Lãi)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("debt_monthly_input")
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = totalMonthsText,
                                onValueChange = { raw ->
                                    totalMonthsText = raw.filter { it.isDigit() }.take(3)
                                },
                                label = { Text("Tổng số tháng vay") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("debt_total_months_input")
                            )

                            OutlinedTextField(
                                value = paidMonthsText,
                                onValueChange = { raw ->
                                    paidMonthsText = raw.filter { it.isDigit() }.take(3)
                                },
                                label = { Text("Số tháng đã đóng") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("debt_paid_months_input")
                            )
                        }

                        OutlinedTextField(
                            value = dueDayText,
                            onValueChange = { raw ->
                                dueDayText = raw.filter { it.isDigit() }.take(2)
                            },
                            label = { Text("Ngày đến hạn trả lãi hàng tháng (1 - 31, VD: 15)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Khung tự động tính toán trực quan ngay trên Form
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MintLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "TỰ ĐỘNG TÍNH TOÁN LỊCH TRẢ NỢ:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForestGreen
                                )
                                Text(
                                    text = "• Đã đóng: $parsedPaidMonths / $parsedTotalMonths tháng • Còn lại: $remainingMonthsPreview tháng",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "• Trả hàng tháng: ${formatter.format(parsedMonthly)} đ • Dư nợ còn lại: ${formatter.format(remainingMoneyPreview)} đ",
                                    fontSize = 11.sp,
                                    color = DangerCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Ghi chú hợp đồng / lãi suất (tùy chọn)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            val totalMonths = (totalMonthsText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            val paidMonths = (paidMonthsText.toIntOrNull() ?: 0).coerceIn(0, totalMonths)
                            val dueDay = (dueDayText.toIntOrNull() ?: 15).coerceIn(1, 31)
                            val monthlyInstallment = monthlyInstallmentText.toLongOrNull()
                                ?: (amount / totalMonths.toLong()).coerceAtLeast(1L)

                            if (personName.isNotBlank() && amount > 0L) {
                                viewModel.addLoanDebt(
                                    loanType = loanType,
                                    lenderName = personName.trim(),
                                    totalLoanAmount = amount,
                                    monthlyInstallmentAmount = monthlyInstallment,
                                    totalMonths = totalMonths,
                                    paidMonths = paidMonths,
                                    dueDayOfMonth = dueDay,
                                    note = note.ifBlank { "Hạn trả ngày $dueDay hàng tháng" }
                                )
                                showAddDebtDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.testTag("debt_save_button")
                    ) {
                        Text("Lưu Khoản Vay", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDebtDialog = false }) {
                        Text("Hủy", color = TextSecondary)
                    }
                },
                containerColor = CardWhite
            )
        }

        // ==================== DIALOG 3: TẠO QUỸ TIẾT KIỆM MỤC TIÊU ====================
        if (showAddGoalDialog) {
            var title by remember { mutableStateOf("") }
            var targetText by remember { mutableStateOf("") }
            var selectedIcon by remember { mutableStateOf("CAR") }
            var note by remember { mutableStateOf("") }

            FinanceModalDialog(
                onDismissRequest = { showAddGoalDialog = false },
                title = {
                    Text("Tạo Quỹ Tiết Kiệm Mục Tiêu", fontWeight = FontWeight.Black, color = ForestGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tên quỹ (VD: Mua xe, Đóng học, Mua Laptop)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Số tiền mục tiêu (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Loại mục tiêu:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "CAR" to "Phương tiện",
                                "SCHOOL" to "Học tập",
                                "SAVINGS" to "Tích lũy"
                            ).forEach { (code, label) ->
                                FilterChip(
                                    selected = selectedIcon == code,
                                    onClick = { selectedIcon = code },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Cam kết kỷ luật cho quỹ này") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = targetText.toLongOrNull() ?: 0L
                            if (title.isNotBlank() && target > 0L) {
                                viewModel.addSavingsGoal(
                                    title = title.trim(),
                                    targetAmount = target,
                                    categoryIcon = selectedIcon,
                                    note = note.ifBlank { "Cam kết trích thu nhập định kỳ" }
                                )
                                showAddGoalDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Tạo Quỹ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGoalDialog = false }) {
                        Text("Hủy", color = TextSecondary)
                    }
                },
                containerColor = CardWhite
            )
        }

        // ==================== DIALOG 4: TRÍCH TIỀN TỪ THU NHẬP VÀO QUỸ ====================
        selectedGoalForDeposit?.let { goal ->
            var depositText by remember { mutableStateOf("500000") }
            FinanceModalDialog(
                onDismissRequest = { selectedGoalForDeposit = null },
                title = {
                    Text("Trích Tiền Vào ${goal.title}", fontWeight = FontWeight.Black, color = ForestGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Hiện có: ${formatter.format(goal.currentAmount)} / ${formatter.format(goal.targetAmount)} đ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(200_000L to "+200k", 500_000L to "+500k", 1_000_000L to "+1 Tr", 2_000_000L to "+2 Tr").forEach { (amt, label) ->
                                FilterChip(
                                    selected = depositText == amt.toString(),
                                    onClick = { depositText = amt.toString() },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LeafGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = depositText,
                            onValueChange = { depositText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Số tiền trích vào quỹ (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = depositText.toLongOrNull() ?: 0L
                            if (amt > 0L) {
                                viewModel.allocateToSavingsGoal(goal.id, goal.title, amt)
                                selectedGoalForDeposit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Xác Nhận Trích Quỹ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedGoalForDeposit = null }) {
                        Text("Hủy", color = TextSecondary)
                    }
                },
                containerColor = CardWhite
            )
        }

        // ==================== DIALOG 5: PHẦN 2 - THÊM VÍ ĐIỆN TỬ / NGÂN HÀNG MỚI ====================
        if (showAddWalletDialog) {
            var walletName by remember { mutableStateOf("") }
            var initialBalanceText by remember { mutableStateOf("0") }
            var selectedIconType by remember { mutableStateOf("MOMO") }

            val quickPresets = listOf(
                Triple("MoMo", "MOMO", "momo,com.mservice.momotransfer"),
                Triple("Cake", "CAKE", "cake,vn.cake"),
                Triple("Vietcombank", "VCB", "vietcombank,vcb,com.vcb"),
                Triple("MB Bank", "MB", "mb bank,mbbank,com.mbmobile"),
                Triple("TNEX", "TNEX", "tnex,com.msb.tnex"),
                Triple("TPBank", "TPB", "tpbank,tpb,com.tpb.mb.gprsandroid"),
                Triple("Techcombank", "TCB", "techcombank,tcb"),
                Triple("ZaloPay", "ZALOPAY", "zalopay,vn.com.vng.zalopay")
            )

            FinanceModalDialog(
                onDismissRequest = { showAddWalletDialog = false },
                title = {
                    Text("Thêm Ví Điện Tử / Ngân Hàng", fontWeight = FontWeight.Black, color = ForestGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Chọn nhanh Ví/Ngân hàng phổ biến:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickPresets.forEach { (presetName, iconCode, _) ->
                                FilterChip(
                                    selected = walletName.equals(presetName, ignoreCase = true),
                                    onClick = {
                                        walletName = presetName
                                        selectedIconType = iconCode
                                    },
                                    label = { Text(presetName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = walletName,
                            onValueChange = { walletName = it },
                            label = { Text("Tên Ví / Ngân hàng (VD: MoMo, Cake, VCB...)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wallet_name_input")
                        )

                        OutlinedTextField(
                            value = initialBalanceText,
                            onValueChange = { raw ->
                                initialBalanceText = raw.filter { it.isDigit() }.take(15)
                            },
                            label = { Text("Số dư hiện tại (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wallet_balance_input")
                        )

                        Text(
                            text = "Khi có thông báo biến động số dư từ ứng dụng này, hệ thống sẽ tự động cộng/trừ vào số dư của ví.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val balance = initialBalanceText.toLongOrNull() ?: 0L
                            if (walletName.isNotBlank()) {
                                val matchedPreset = quickPresets.firstOrNull {
                                    it.first.equals(walletName.trim(), ignoreCase = true)
                                }
                                viewModel.addWallet(
                                    name = walletName.trim(),
                                    balance = balance,
                                    iconType = matchedPreset?.second ?: selectedIconType,
                                    packageKeywords = matchedPreset?.third ?: walletName.trim().lowercase()
                                )
                                showAddWalletDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.testTag("wallet_save_button")
                    ) {
                        Text("Thêm Ví / NH", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddWalletDialog = false }) {
                        Text("Hủy", color = TextSecondary)
                    }
                },
                containerColor = CardWhite
            )
        }

        // ==================== DIALOG 6: CẬP NHẬT SỐ DƯ / XÓA VÍ ====================
        selectedWalletForEdit?.let { wallet ->
            var newBalanceText by remember(wallet) { mutableStateOf(wallet.balance.toString()) }
            FinanceModalDialog(
                onDismissRequest = { selectedWalletForEdit = null },
                title = {
                    Text("Quản Lý Ví: ${wallet.name}", fontWeight = FontWeight.Black, color = ForestGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Số dư hiện tại: ${formatter.format(wallet.balance)} đ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                        OutlinedTextField(
                            value = newBalanceText,
                            onValueChange = { raw ->
                                newBalanceText = raw.filter { it.isDigit() }.take(15)
                            },
                            label = { Text("Điều chỉnh số dư thực tế (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteWallet(wallet.id)
                                selectedWalletForEdit = null
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Xóa Ví / Ngân Hàng Này", color = DangerCrimson, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updated = newBalanceText.toLongOrNull() ?: wallet.balance
                            viewModel.updateWalletBalance(wallet.id, updated)
                            selectedWalletForEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Lưu Số Dư", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedWalletForEdit = null }) {
                        Text("Đóng", color = TextSecondary)
                    }
                },
                containerColor = CardWhite
            )
        }

        // ==================== DIALOG 7: PHẦN 4 - CHI TIẾT LỊCH SỬ GIAO DỊCH (MODAL DETAIL SHEET) ====================
        selectedTransactionForDetail?.let { tx ->
            val isIncome = tx.type == "INCOME"
            val exactDateTime = remember(tx.timestamp) {
                try {
                    SimpleDateFormat("dd/MM/yyyy • HH:mm:ss", Locale("vi", "VN")).format(Date(tx.timestamp))
                } catch (_: Exception) {
                    "Hôm nay"
                }
            }

            FinanceModalDialog(
                onDismissRequest = { selectedTransactionForDetail = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isIncome) SuccessGreen.copy(alpha = 0.14f) else DangerCrimson.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isIncome) SuccessGreen else DangerCrimson
                            )
                        }
                        Column {
                            Text(
                                text = "CHI TIẾT GIAO DỊCH",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = ForestGreen
                            )
                            Text(
                                text = if (isIncome) "Biến động tăng (+)" else "Biến động giảm (-)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_detail_sheet")
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isIncome) SuccessGreen.copy(alpha = 0.08f) else DangerCrimson.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "${if (isIncome) "+" else "-"}${formatter.format(tx.amount)} đ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isIncome) SuccessGreen else DangerCrimson
                                )
                                if (tx.savingsAllocated > 0L) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Đã tự động trích 10% Quỹ Kỷ Luật: ${formatter.format(tx.savingsAllocated)} đ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder)

                        TransactionDetailRow(label = "Ngày giờ chính xác", value = exactDateTime)
                        TransactionDetailRow(label = "Danh mục", value = tx.category)
                        TransactionDetailRow(label = "Ví / Ngân hàng nguồn", value = tx.bankSource)
                        TransactionDetailRow(
                            label = "Nội dung ghi chú",
                            value = tx.description.ifBlank { "Không có ghi chú" }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedTransactionForDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Đóng", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(tx.id)
                            selectedTransactionForDetail = null
                        }
                    ) {
                        Text("Xóa Giao Dịch", color = DangerCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CardWhite
            )
        }
    }
}

@Composable
private fun TransactionDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * PHẦN 2: Thẻ quản lý danh sách Ví điện tử (MoMo, Cake, ZaloPay) & Ngân hàng
 */
@Composable
private fun WalletManagementSectionCard(
    wallets: List<AccountWalletEntity>,
    formatter: NumberFormat,
    onAddWalletClick: () -> Unit,
    onWalletClick: (AccountWalletEntity) -> Unit
) {
    val totalWalletsBalance = remember(wallets) { wallets.sumOf { it.balance } }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = ForestGreen.copy(alpha = 0.12f))
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .testTag("wallet_management_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QUẢN LÝ VÍ & NGÂN HÀNG LIÊN KẾT",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ForestGreen
                    )
                    Text(
                        text = "Tổng số dư các ví: ${formatter.format(totalWalletsBalance)} đ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                OutlinedButton(
                    onClick = onAddWalletClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("add_wallet_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Thêm Ví/NH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (wallets.isEmpty()) {
                Text(
                    text = "Chưa có ví nào. Nhấn 'Thêm Ví/NH' để thêm MoMo, Cake, Vietcombank, MB Bank...",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(wallets, key = { it.id }) { wallet ->
                        val accentColor = when (wallet.iconType.uppercase()) {
                            "MOMO" -> Color(0xFFD82D8B)
                            "CAKE" -> Color(0xFF00B14F)
                            "VCB" -> ForestGreen
                            "MB" -> Color(0xFF1E40AF)
                            "TPB" -> Color(0xFF7C3AED)
                            "TNEX" -> MarineAqua
                            else -> ForestGreenDark
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MintLight.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .width(152.dp)
                                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .clickable { onWalletClick(wallet) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(accentColor)
                                    ) {
                                        Text(
                                            text = wallet.name.take(2).uppercase(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Sửa số dư",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = wallet.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatter.format(wallet.balance)} đ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceStatisticsChartCard(
    transactions: List<FinanceTransactionEntity>,
    todayEpochDay: Long,
    isWeekly: Boolean,
    onTogglePeriod: (Boolean) -> Unit,
    formatter: NumberFormat
) {
    val buckets = remember(transactions, todayEpochDay, isWeekly) {
        if (isWeekly) {
            (6 downTo 0).map { offset ->
                val day = todayEpochDay - offset
                val dayTxs = transactions.filter { it.dateEpochDay == day }
                val inc = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val date = LocalDate.ofEpochDay(day)
                val label = if (offset == 0) "H.Nay" else "${date.dayOfMonth}/${date.monthValue}"
                Triple(label, inc, exp)
            }
        } else {
            (3 downTo 0).map { weekIndex ->
                val endDay = todayEpochDay - (weekIndex * 7)
                val startDay = endDay - 6
                val weekTxs = transactions.filter { it.dateEpochDay in startDay..endDay }
                val inc = weekTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = weekTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val label = if (weekIndex == 0) "Tuần này" else "T-${weekIndex}"
                Triple(label, inc, exp)
            }
        }
    }

    val maxBucketVal = remember(buckets) {
        buckets.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(1_000_000L) ?: 1_000_000L
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = ForestGreen.copy(alpha = 0.12f))
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = "BIỂU ĐỒ THỐNG KÊ THU / CHI",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ForestGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isWeekly) "7 ngày gần nhất" else "4 tuần trong tháng",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MintLight)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isWeekly) ForestGreen else Color.Transparent)
                            .clickable { onTogglePeriod(true) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Tuần",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeekly) Color.White else TextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isWeekly) ForestGreen else Color.Transparent)
                            .clickable { onTogglePeriod(false) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Tháng",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isWeekly) Color.White else TextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val count = buckets.size
                val groupWidth = size.width / count
                val barWidth = (groupWidth * 0.28f).coerceAtMost(24f)
                val maxBarHeight = size.height - 16f

                buckets.forEachIndexed { idx, (_, inc, exp) ->
                    val centerX = idx * groupWidth + groupWidth / 2f

                    val incRatio = (inc.toFloat() / maxBucketVal.toFloat()).coerceIn(0.04f, 1f)
                    val incH = maxBarHeight * incRatio
                    drawRoundRect(
                        color = if (inc > 0) SuccessGreen else MintLight,
                        topLeft = Offset(centerX - barWidth - 3f, size.height - incH),
                        size = Size(barWidth, incH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    val expRatio = (exp.toFloat() / maxBucketVal.toFloat()).coerceIn(0.04f, 1f)
                    val expH = maxBarHeight * expRatio
                    drawRoundRect(
                        color = if (exp > 0) DangerCrimson else MintLight,
                        topLeft = Offset(centerX + 3f, size.height - expH),
                        size = Size(barWidth, expH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                buckets.forEach { (label, _, _) ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SuccessGreen))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thu nhập", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(DangerCrimson))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chi tiêu", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    transactions: List<FinanceTransactionEntity>,
    formatter: NumberFormat
) {
    val expenses = remember(transactions) { transactions.filter { it.type == "EXPENSE" } }
    val totalExp = remember(expenses) { expenses.sumOf { it.amount }.coerceAtLeast(1L) }
    val grouped = remember(expenses) {
        expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PHÂN TÍCH CHI TIÊU THEO DANH MỤC",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (grouped.isEmpty()) {
                Text(
                    text = "Chưa có dữ liệu chi tiêu theo danh mục.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                grouped.forEach { (category, sum) ->
                    val ratio = (sum.toFloat() / totalExp.toFloat()).coerceIn(0f, 1f)
                    val pct = (ratio * 100).roundToInt()

                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = "${formatter.format(sum)} đ ($pct%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerCrimson,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DangerCrimson,
                            trackColor = MintLight,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

/**
 * PHẦN 4: Mỗi item Lịch sử giao dịch có thể click (clickable) để mở Modal xem chi tiết giao dịch
 */
@Composable
private fun TransactionItemCard(
    tx: FinanceTransactionEntity,
    formatter: NumberFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = tx.type == "INCOME"
    val timeText = remember(tx.timestamp) {
        try {
            SimpleDateFormat("dd/MM HH:mm", Locale("vi", "VN")).format(Date(tx.timestamp))
        } catch (_: Exception) {
            ""
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("transaction_item_${tx.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) SuccessGreen.copy(alpha = 0.14f) else DangerCrimson.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) SuccessGreen else DangerCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MintLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.bankSource,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }

                    Text(
                        text = tx.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (timeText.isNotEmpty()) {
                            Text(
                                text = timeText,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        if (tx.savingsAllocated > 0L) {
                            Text(
                                text = "🔒 +${formatter.format(tx.savingsAllocated)} đ (10% Quỹ)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${formatter.format(tx.amount)} đ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isIncome) SuccessGreen else DangerCrimson,
                        maxLines = 1,
                        softWrap = false
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Chi tiết",
                            tint = ForestGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Chi tiết",
                            fontSize = 9.sp,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * PHẦN 3: Card hiển thị trực quan Khoản Vay / Công Nợ với:
 * - Loại khoản vay (Ngân hàng / CT Tài chính / Bạn bè) & Nơi vay
 * - Thanh Progress Bar biểu diễn "Tiến độ trả nợ" (% số tháng đã đóng)
 * - Số tiền còn nợ, Số tiền đóng hàng tháng, Số tháng còn lại
 * - Đếm ngược số ngày tới hạn đóng tiền tháng này (dueDayOfMonth)
 */
@Composable
private fun LoanDebtItemCard(
    debt: DebtEntity,
    formatter: NumberFormat,
    onPayNextMonth: () -> Unit,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = debt.repaymentProgress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "loanRepaymentProgress"
    )
    val progressPct = (progress * 100).roundToInt()
    val daysUntilDue = remember(debt) { debt.daysUntilNextDueDate() }
    val isUrgentDue = !debt.isSettled && daysUntilDue <= 3L

    val badgeColor = when (debt.type.uppercase()) {
        "BANK", "BANK_LOAN" -> ForestGreen
        "FINANCE_COMPANY", "FINANCE" -> WarmAmber
        else -> MarineAqua
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (debt.isSettled) MintLight.copy(alpha = 0.6f) else CardWhite
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = badgeColor.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                color = if (isUrgentDue) DangerCrimson.copy(alpha = 0.6f) else CardBorder,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Lender Name, Loan Type Badge & Due Day Countdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = when (debt.type.uppercase()) {
                                "BANK", "BANK_LOAN" -> Icons.Default.AccountBalance
                                "FINANCE_COMPANY", "FINANCE" -> Icons.Default.Payments
                                else -> Icons.Default.Handshake
                            },
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = debt.loanTypeLabelVi,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = badgeColor
                                )
                            }
                            if (debt.isSettled) {
                                Text(
                                    text = "✅ ĐÃ TẤT TOÁN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Due date countdown pill
                if (!debt.isSettled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isUrgentDue) DangerCrimson.copy(alpha = 0.14f) else MintLight
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (isUrgentDue) DangerCrimson else ForestGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (daysUntilDue == 0L) {
                                    "Đến hạn hôm nay (Ngày ${debt.dueDayOfMonth})"
                                } else {
                                    "Hạn ngày ${debt.dueDayOfMonth} • Còn $daysUntilDue ngày"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUrgentDue) DangerCrimson else ForestGreenDark,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Column Financial Metrics: Tổng vay | Trả hàng tháng | Còn nợ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintLight.copy(alpha = 0.6f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TỔNG TIỀN VAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = "${formatter.format(debt.amount)} đ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TRẢ HÀNG THÁNG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = "${formatter.format(if (debt.monthlyInstallment > 0L) debt.monthlyInstallment else debt.amount)} đ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = WarmAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("SỐ TIỀN CÒN NỢ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = "${formatter.format(debt.remainingDebtAmount)} đ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (debt.isSettled) SuccessGreen else DangerCrimson,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Repayment Progress Bar (% số tháng đã đóng & số tháng còn lại)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tiến độ trả nợ: Đã đóng ${debt.paidMonths}/${debt.totalMonths} tháng ($progressPct%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenDark
                )
                Text(
                    text = if (debt.isSettled) "Hoàn tất" else "Còn ${debt.remainingMonths} tháng",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (debt.isSettled) SuccessGreen else DangerCrimson
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (debt.isSettled) SuccessGreen else ForestGreen,
                trackColor = MintLight,
                strokeCap = StrokeCap.Round
            )

            if (debt.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ghi chú: ${debt.note}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Đóng kỳ tháng này | Tất toán | Xóa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!debt.isSettled && debt.paidMonths < debt.totalMonths) {
                    Button(
                        onClick = onPayNextMonth,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Đóng Kỳ Tháng ${debt.paidMonths + 1}/${debt.totalMonths}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToggleSettled,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (debt.isSettled) "Mở lại" else "Tất toán",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa khoản vay",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalItemCard(
    goal: SavingsGoalEntity,
    formatter: NumberFormat,
    onDepositClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val ratio = (goal.currentAmount.toFloat() / goal.targetAmount.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "goalProgress"
    )

    val icon = when (goal.categoryIcon) {
        "CAR" -> Icons.Default.DirectionsCar
        "SCHOOL" -> Icons.Default.School
        else -> Icons.Default.Savings
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = ForestGreen.copy(alpha = 0.1f))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintLight)
                    ) {
                        Icon(icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (goal.note.isNotBlank()) {
                            Text(
                                text = goal.note,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(ratio * 100).roundToInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (ratio >= 1f) SuccessGreen else ForestGreen,
                        modifier = Modifier
                            .background(MintLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa quỹ", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${formatter.format(goal.currentAmount)} đ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ForestGreenDark
                )
                Text(
                    text = "/ ${formatter.format(goal.targetAmount)} đ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (ratio >= 1f) SuccessGreen else LeafGreen,
                trackColor = MintLight,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDepositClick,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Trích Thu Nhập Vào Quỹ Này",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            }
        }
    }
}

@Composable
private fun EmptyFinanceStateCard(
    title: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FinanceModalDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    containerColor: Color = CardWhite
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismissRequest)
            .padding(20.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                title()
                text()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}
