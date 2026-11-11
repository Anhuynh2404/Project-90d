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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.Project90ViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private enum class FinanceSubTab(val title: String) {
    MONEY("Thu Chi & Biểu Đồ"),
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
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val totalSavings by viewModel.totalSavings.collectAsStateWithLifecycle()
    val totalGoalsSaved by viewModel.totalGoalsSaved.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(FinanceSubTab.MONEY) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    // Chart period toggle: true = 7 days (Tuần), false = 30 days (Tháng)
    var isWeeklyChart by remember { mutableStateOf(true) }

    val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    LazyColumn(
        modifier = modifier
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
                        text = "Quản lý Thu Chi • Công Nợ • Quỹ Mục Tiêu",
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
                            FinanceSubTab.DEBT -> "Thêm Khoản Nợ"
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

        // ==================== TAB 1: QUẢN LÝ TIỀN (THU/CHI & BIỂU ĐỒ) ====================
        if (activeSubTab == FinanceSubTab.MONEY) {
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

                    // Net balance & 10% savings strip
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

            // Bank Notification Listener Automation Card
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MintLight)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tự Động Đọc Biến Động Số Dư Ngân Hàng",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "TNEX • MB Bank • Vietcombank • Techcombank",
                                    fontSize = 11.sp,
                                    color = ForestGreen
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
                            Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = ForestGreen)
                        }
                    }
                }
            }

            // Transaction List Header
            item {
                Text(
                    text = "LỊCH SỬ GIAO DỊCH CHI TIẾT (${transactions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyFinanceStateCard(
                        title = "Chưa có giao dịch thu/chi nào",
                        subtitle = "Nhấn 'Ghi Thu/Chi' ở góc trên để thêm khoản thu nhập hoặc chi tiêu đầu tiên."
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        tx = tx,
                        formatter = formatter,
                        onDelete = { viewModel.deleteTransaction(tx.id) }
                    )
                }
            }
        }

        // ==================== TAB 2: QUẢN LÝ NỢ (CHO VAY / ĐI MƯỢN) ====================
        if (activeSubTab == FinanceSubTab.DEBT) {
            item {
                val totalLend = debts.filter { it.type == "LEND" && !it.isSettled }.sumOf { it.amount - it.paidAmount }
                val totalBorrow = debts.filter { it.type == "BORROW" && !it.isSettled }.sumOf { it.amount - it.paidAmount }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = MarineAqua.copy(alpha = 0.14f))
                            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ĐANG CHO VAY (PHẢI THU)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarineAqua)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${formatter.format(totalLend)} đ",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = MarineAqua
                            )
                            Text(
                                text = "${debts.count { it.type == "LEND" && !it.isSettled }} khoản chưa thu",
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
                            Text("ĐANG ĐI MƯỢN (PHẢI TRẢ)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarmAmber)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${formatter.format(totalBorrow)} đ",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = DangerCrimson
                            )
                            Text(
                                text = "${debts.count { it.type == "BORROW" && !it.isSettled }} khoản cần trả",
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
                        title = "Sổ quản lý nợ đang trống",
                        subtitle = "Ghi chép rõ ràng các khoản Cho Vay hoặc Đi Mượn, người liên quan và hạn thanh toán để giữ uy tín tài chính."
                    )
                }
            } else {
                items(debts, key = { it.id }) { debt ->
                    DebtItemCard(
                        debt = debt,
                        todayEpochDay = todayEpoch,
                        formatter = formatter,
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

    // ==================== DIALOG 1: GHI THU / CHI MỚI ====================
    if (showAddTxDialog) {
        var amountText by remember { mutableStateOf("") }
        var isIncome by remember { mutableStateOf(false) }
        var isSalary by remember { mutableStateOf(false) }
        val categories = if (isIncome) {
            listOf("Lương", "Thưởng / Freelance", "Đầu tư sinh lời", "Thu khác")
        } else {
            listOf("Ăn uống", "Tập gym & Sức khỏe", "Di chuyển", "Học tập & Sách", "Sinh hoạt", "Khác")
        }
        var selectedCategory by remember(isIncome) { mutableStateOf(categories.first()) }
        var bankSource by remember { mutableStateOf("TNEX") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
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
                            Text("Chi Tiêu", color = if (!isIncome) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isIncome = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) SuccessGreen else MintLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Thu Nhập", color = if (isIncome) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Số tiền (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Chọn danh mục:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
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
                        value = bankSource,
                        onValueChange = { bankSource = it },
                        label = { Text("Nguồn tiền (TNEX, MB Bank, Tiền mặt)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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
                                description = description.ifBlank { selectedCategory },
                                isSalary = isSalary
                            )
                            showAddTxDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
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

    // ==================== DIALOG 2: THÊM KHOẢN NỢ (CHO VAY / ĐI MƯỢN) ====================
    if (showAddDebtDialog) {
        var isLend by remember { mutableStateOf(true) }
        var personName by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var dueDays by remember { mutableIntStateOf(14) }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDebtDialog = false },
            title = {
                Text("Ghi Chép Khoản Nợ Mới", fontWeight = FontWeight.Black, color = ForestGreen)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { isLend = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isLend) MarineAqua else MintLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cho Vay", color = if (isLend) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isLend = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isLend) WarmAmber else MintLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Đi Mượn", color = if (!isLend) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Người liên quan (Họ tên)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Số tiền (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Hạn thanh toán:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(7 to "7 ngày", 14 to "14 ngày", 30 to "1 tháng", 60 to "2 tháng").forEach { (days, label) ->
                            FilterChip(
                                selected = dueDays == days,
                                onClick = { dueDays = days },
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
                        label = { Text("Ghi chú (Số tài khoản / Lý do)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (personName.isNotBlank() && amount > 0L) {
                            viewModel.addDebt(
                                type = if (isLend) "LEND" else "BORROW",
                                personName = personName.trim(),
                                amount = amount,
                                dueDateEpochDay = todayEpoch + dueDays,
                                note = note.ifBlank { if (isLend) "Khoản cho vay" else "Khoản đi mượn" }
                            )
                            showAddDebtDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Lưu Khoản Nợ", color = Color.White, fontWeight = FontWeight.Bold)
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

        AlertDialog(
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
        AlertDialog(
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
}

@Composable
private fun FinanceStatisticsChartCard(
    transactions: List<FinanceTransactionEntity>,
    todayEpochDay: Long,
    isWeekly: Boolean,
    onTogglePeriod: (Boolean) -> Unit,
    formatter: NumberFormat
) {
    // Prepare 7-day or 4-week buckets
    val buckets = remember(transactions, todayEpochDay, isWeekly) {
        if (isWeekly) {
            (6 downTo 0).map { offset ->
                val day = todayEpochDay - offset
                val date = LocalDate.ofEpochDay(day)
                val label = if (offset == 0) "H.Nay" else "T${date.dayOfWeek.value + 1}".replace("T8", "CN")
                val dayTxs = transactions.filter { it.dateEpochDay == day }
                val inc = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                Triple(label, inc, exp)
            }
        } else {
            (3 downTo 0).map { weekIdx ->
                val endDay = todayEpochDay - (weekIdx * 7)
                val startDay = endDay - 6
                val label = "Tuần ${4 - weekIdx}"
                val periodTxs = transactions.filter { it.dateEpochDay in startDay..endDay }
                val inc = periodTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = periodTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                Triple(label, inc, exp)
            }
        }
    }

    val maxVal = remember(buckets) {
        buckets.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(1_000_000L) ?: 1_000_000L
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = ForestGreen.copy(alpha = 0.12f))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "BIỂU ĐỒ THỐNG KÊ THU / CHI",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ForestGreen
                    )
                    Text(
                        text = if (isWeekly) "Biến động 7 ngày gần nhất" else "Biến động 4 tuần trong tháng",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MintLight)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isWeekly) ForestGreen else Color.Transparent)
                            .clickable { onTogglePeriod(true) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Tuần", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isWeekly) Color.White else ForestGreen)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isWeekly) ForestGreen else Color.Transparent)
                            .clickable { onTogglePeriod(false) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Tháng", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isWeekly) Color.White else ForestGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Bar Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val count = buckets.size
                    val slotWidth = size.width / count
                    val barWidth = (slotWidth * 0.28f).coerceAtMost(22.dp.toPx())
                    val chartHeight = size.height - 8.dp.toPx()

                    // Horizontal guide lines
                    for (i in 1..3) {
                        val y = chartHeight * (1f - i / 3f)
                        drawLine(
                            color = CardBorder.copy(alpha = 0.6f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    buckets.forEachIndexed { idx, (_, inc, exp) ->
                        val centerX = idx * slotWidth + slotWidth / 2f
                        val incRatio = (inc.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                        val expRatio = (exp.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)

                        val incHeight = chartHeight * incRatio
                        val expHeight = chartHeight * expRatio

                        // Income Bar (Green)
                        drawRoundRect(
                            color = SuccessGreen,
                            topLeft = Offset(centerX - barWidth - 2.dp.toPx(), chartHeight - incHeight),
                            size = Size(barWidth, incHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Expense Bar (Crimson)
                        drawRoundRect(
                            color = DangerCrimson,
                            topLeft = Offset(centerX + 2.dp.toPx(), chartHeight - expHeight),
                            size = Size(barWidth, expHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // X-axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                buckets.forEach { (label, _, _) ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
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
    val expenseByCategory = remember(transactions) {
        val expenses = transactions.filter { it.type == "EXPENSE" }
        val totalExp = expenses.sumOf { it.amount }.coerceAtLeast(1L)
        expenses.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                Triple(cat, sum, (sum.toFloat() / totalExp.toFloat()).coerceIn(0f, 1f))
            }
            .sortedByDescending { it.second }
    }

    if (expenseByCategory.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PHÂN BỔ CHI TIÊU THEO DANH MỤC",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = ForestGreen
                )

                expenseByCategory.take(5).forEach { (cat, sum, ratio) ->
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "${formatter.format(sum)} đ (${(ratio * 100).roundToInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DangerCrimson
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = WarmAmber,
                            trackColor = MintLight,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(
    tx: FinanceTransactionEntity,
    formatter: NumberFormat,
    onDelete: () -> Unit
) {
    val isIncome = tx.type == "INCOME"
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isIncome) SuccessGreen.copy(alpha = 0.14f) else DangerCrimson.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = tx.type,
                        tint = if (isIncome) SuccessGreen else DangerCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tx.bankSource,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier
                                .background(MintLight, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (tx.description.isNotBlank()) {
                        Text(
                            text = tx.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (tx.savingsAllocated > 0) {
                        Text(
                            text = "Đã trích +${formatter.format(tx.savingsAllocated)}đ vào quỹ tiết kiệm",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmAmber
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if (isIncome) "+" else "-") + formatter.format(tx.amount) + " đ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isIncome) SuccessGreen else DangerCrimson,
                    maxLines = 1,
                    softWrap = false
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DebtItemCard(
    debt: DebtEntity,
    todayEpochDay: Long,
    formatter: NumberFormat,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    val isLend = debt.type == "LEND"
    val daysRemaining = debt.dueDateEpochDay - todayEpochDay
    val dueDateFormatted = remember(debt.dueDateEpochDay) {
        LocalDate.ofEpochDay(debt.dueDateEpochDay).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = if (debt.isSettled) MintSurface else CardWhite),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (debt.isSettled) SuccessGreen.copy(alpha = 0.4f) else CardBorder,
                RoundedCornerShape(18.dp)
            )
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isLend) MarineAqua.copy(alpha = 0.14f)
                                else WarmAmber.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = if (isLend) MarineAqua else WarmAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = debt.personName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLend) "CHO VAY" else "ĐI MƯỢN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLend) MarineAqua else DangerCrimson,
                                modifier = Modifier
                                    .background(
                                        if (isLend) MarineAqua.copy(alpha = 0.12f) else DangerCrimson.copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Hạn trả: $dueDateFormatted • " + when {
                                debt.isSettled -> "Đã tất toán xong"
                                daysRemaining < 0 -> "Quá hạn ${-daysRemaining} ngày!"
                                daysRemaining == 0L -> "Đến hạn hôm nay!"
                                else -> "Còn $daysRemaining ngày"
                            },
                            fontSize = 11.sp,
                            color = if (!debt.isSettled && daysRemaining < 0) DangerCrimson else TextSecondary,
                            fontWeight = if (!debt.isSettled && daysRemaining <= 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            if (debt.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = debt.note, fontSize = 12.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${formatter.format(debt.amount)} đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (debt.isSettled) SuccessGreen else if (isLend) MarineAqua else DangerCrimson
                )

                Button(
                    onClick = onToggleSettled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (debt.isSettled) SuccessGreen else ForestGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (debt.isSettled) "Đã Tất Toán" else "Đánh Dấu Đã Trả",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
    val ratio = if (goal.targetAmount > 0L) {
        (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "goalProgress_${goal.id}"
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintLight)
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
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
