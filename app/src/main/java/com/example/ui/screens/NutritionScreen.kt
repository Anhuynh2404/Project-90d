package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NoDrinks
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.EcoBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.Project90ViewModel
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

data class FoodGroupCategory(
    val id: String,
    val title: String,
    val badge: String,
    val subtitle: String,
    val accentColor: Color,
    val icon: ImageVector,
    val foods: List<FoodDishItem>
)

data class FoodDishItem(
    val name: String,
    val portion: String,
    val macros: String,
    val benefit: String
)

@Composable
fun NutritionScreen(
    viewModel: Project90ViewModel,
    modifier: Modifier = Modifier
) {
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val logsMap = remember(todayLogs) { todayLogs.associateBy { it.habitId } }

    val noSugarLog = logsMap["no_sugar"]
    val isNoSugarDone = noSugarLog?.isCompleted == true

    val waterLog = logsMap["water_2l"]
    val waterProgress = waterLog?.progressValue ?: 0
    val waterGoal = 2000
    val waterPercent = (waterProgress.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f)

    // Animated water level for rising wave effect
    val animatedWaterLevel by animateFloatAsState(
        targetValue = waterPercent,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "waterRisingLevel"
    )

    // Infinite wave phase shift
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val foodGroups = remember {
        listOf(
            FoodGroupCategory(
                id = "CLEAN_EATING",
                title = "Clean Eating",
                badge = "Nguyên Bản & Tự Nhiên",
                subtitle = "Tinh bột hấp thu chậm & chất béo thực vật tinh khiết",
                accentColor = ForestGreen,
                icon = Icons.Default.Spa,
                foods = listOf(
                    FoodDishItem(
                        name = "Yến Mạch Nguyên Cám & Hạt Chia",
                        portion = "60g buổi sáng",
                        macros = "220 kcal • Giàu Beta-Glucan",
                        benefit = "Giải phóng năng lượng đều đặn suốt 4 tiếng, ổn định đường huyết tuyệt đối."
                    ),
                    FoodDishItem(
                        name = "Khoai Lang Mật Luộc / Nướng",
                        portion = "150g bữa trưa",
                        macros = "130 kcal • Chỉ số GI thấp",
                        benefit = "Thay thế cơm trắng tinh luyện, giàu chất xơ hòa tan và Vitamin A."
                    ),
                    FoodDishItem(
                        name = "Quả Bơ Sáp & Dầu Olive Ép Lạnh",
                        portion = "1/2 quả bơ / ngày",
                        macros = "160 kcal • Omega-9",
                        benefit = "Nuôi dưỡng màng tế bào thần kinh, hỗ trợ hấp thu vitamin tan trong dầu."
                    )
                )
            ),
            FoodGroupCategory(
                id = "HIGH_PROTEIN",
                title = "High Protein",
                badge = "Xây Cơ & Đốt Mỡ",
                subtitle = "Nguồn đạm tinh khiết sinh nhiệt cao, triệt tiêu cơn thèm ngọt",
                accentColor = MarineAqua,
                icon = Icons.Default.FitnessCenter,
                foods = listOf(
                    FoodDishItem(
                        name = "Ức Gà Phi Lê Áp Chảo Thảo Mộc",
                        portion = "200g / bữa chính",
                        macros = "62g Protein • <3g Fat",
                        benefit = "Tối đa hóa tổng hợp protein cơ bắp (MPS) với lượng mỡ thừa gần như bằng 0."
                    ),
                    FoodDishItem(
                        name = "Cá Hồi Na Uy / Cá Thu Đại Dương",
                        portion = "180g / bữa tối",
                        macros = "40g Protein • Giàu EPA/DHA",
                        benefit = "Kháng viêm khớp sau tập nặng, tăng độ nhạy Insulin và bảo vệ tim mạch."
                    ),
                    FoodDishItem(
                        name = "Trứng Gà Ta Luộc Lòng Đào",
                        portion = "3 - 4 quả / ngày",
                        macros = "24g Protein • Giàu Choline",
                        benefit = "Nguồn axit amin chuẩn vàng (BV = 100), tăng cường dẫn truyền thần kinh buổi sáng."
                    )
                )
            ),
            FoodGroupCategory(
                id = "LOW_CARB",
                title = "Low Carb",
                badge = "Siết Mỡ Nội Tạng",
                subtitle = "Rau họ cải & thực phẩm mật độ dinh dưỡng cao, ít tinh bột",
                accentColor = LeafGreen,
                icon = Icons.Default.Grass,
                foods = listOf(
                    FoodDishItem(
                        name = "Bông Cải Xanh (Súp Lơ) Hấp",
                        portion = "250g / ngày",
                        macros = "85 kcal • Sulforaphane",
                        benefit = "Kích hoạt enzyme giải độc gan tự nhiên và cung cấp lượng lớn Vitamin C, K."
                    ),
                    FoodDishItem(
                        name = "Cải Bó Xôi (Spinach) & Măng Tây",
                        portion = "200g / bữa",
                        macros = "45 kcal • Giàu Magie & Sắt",
                        benefit = "Bổ sung điện giải tự nhiên, thư giãn hệ thần kinh và làm phẳng vùng bụng."
                    ),
                    FoodDishItem(
                        name = "Nấm Đùi Gà & Đậu Phụ Non",
                        portion = "150g / bữa tối",
                        macros = "110 kcal • Đạm thực vật",
                        benefit = "Nhẹ bụng buổi tối, giúp hệ tiêu hóa nghỉ ngơi sớm trước giờ giới nghiêm 22:30."
                    )
                )
            ),
            FoodGroupCategory(
                id = "MUSCLE_RECOVERY",
                title = "Phục Hồi Cơ",
                badge = "Sau Tập Luyện",
                subtitle = "Tái tạo Glycogen, giảm đau nhức cơ DOMS và bù điện giải",
                accentColor = WarmAmber,
                icon = Icons.Default.Bolt,
                foods = listOf(
                    FoodDishItem(
                        name = "Sữa Chua Hy Lạp Không Đường + Việt Quất",
                        portion = "150g sau tập",
                        macros = "18g Casein & Whey • Probiotics",
                        benefit = "Chống oxy hóa mạnh mẽ, phục hồi các sợi cơ vi thể bị xé nhỏ khi tập nặng."
                    ),
                    FoodDishItem(
                        name = "Chuối Chín & Hạnh Nhân Rang Mộc",
                        portion = "1 quả chuối + 15 hạt",
                        macros = "450mg Kali • Magie tự nhiên",
                        benefit = "Chống chuột rút tức thì, bù đắp nhanh dự trữ glycogen cơ bắp sau chạy bộ/đá bóng."
                    ),
                    FoodDishItem(
                        name = "Thịt Bò Thăn Nội (Sirloin) Nướng",
                        portion = "150g / bữa sau tập",
                        macros = "42g Protein • Creatine & Kẽm",
                        benefit = "Tái tạo ATP năng lượng tế bào và thúc đẩy hormone tăng trưởng tự nhiên."
                    )
                )
            )
        )
    }

    var selectedGroupId by remember { mutableStateOf(foodGroups.first().id) }
    val activeFoodGroup = remember(selectedGroupId) {
        foodGroups.first { it.id == selectedGroupId }
    }

    val milestones = listOf(500, 1000, 1500, 2000)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EcoBackground)
            .testTag("nutrition_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "MÔ-ĐUN DINH DƯỠNG & HYDRAT HÓA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = ForestGreen
                )
                Text(
                    text = "Kỷ luật No Sugar • Sóng nước 2L • Thực đơn 4 nhóm chuẩn khoa học",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // 1. Water Tracker Card with Rising Wave Animation & 100ml / 250ml / 500ml Shortcuts
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = MarineAqua.copy(alpha = 0.16f))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(18.dp)
                ) {
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MarineAqua.copy(alpha = 0.14f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalDrink,
                                    contentDescription = "Water",
                                    tint = MarineAqua,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BỂ NƯỚC KỶ LUẬT 2.000 ML",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Hiệu ứng nước dâng theo từng cột mốc",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MintLight)
                                .clickable { viewModel.updateHabitProgress("water_2l", 0, 2000) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Đặt lại",
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular Rising Wave Tank
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(190.dp)
                            .shadow(8.dp, CircleShape, ambientColor = MarineAqua.copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(Color(0xFFEFF8FA))
                            .border(3.dp, MarineAqua.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val circlePath = Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                            }

                            clipPath(circlePath) {
                                val waterTopY = size.height * (1f - animatedWaterLevel)
                                val waveAmplitude = if (animatedWaterLevel in 0.02f..0.98f) 8.dp.toPx() else 2.dp.toPx()

                                // Back Wave (Lighter Aqua)
                                val backWavePath = Path().apply {
                                    moveTo(0f, size.height)
                                    lineTo(0f, waterTopY)
                                    var x = 0f
                                    while (x <= size.width) {
                                        val angle = (x / size.width) * (2f * PI).toFloat() + wavePhase + 1.4f
                                        val y = waterTopY + sin(angle) * waveAmplitude
                                        lineTo(x, y)
                                        x += 6f
                                    }
                                    lineTo(size.width, size.height)
                                    close()
                                }
                                drawPath(
                                    path = backWavePath,
                                    color = MarineAqua.copy(alpha = 0.35f)
                                )

                                // Front Wave (Deep Marine -> Leaf Green gradient)
                                val frontWavePath = Path().apply {
                                    moveTo(0f, size.height)
                                    lineTo(0f, waterTopY)
                                    var x = 0f
                                    while (x <= size.width) {
                                        val angle = (x / size.width) * (2f * PI).toFloat() - wavePhase
                                        val y = waterTopY + sin(angle) * waveAmplitude
                                        lineTo(x, y)
                                        x += 6f
                                    }
                                    lineTo(size.width, size.height)
                                    close()
                                }
                                drawPath(
                                    path = frontWavePath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MarineAqua.copy(alpha = 0.78f),
                                            ForestGreen.copy(alpha = 0.88f)
                                        ),
                                        startY = waterTopY,
                                        endY = size.height
                                    )
                                )
                            }
                        }

                        // Text inside Wave Tank
                        val isOverHalf = animatedWaterLevel >= 0.55f
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(waterPercent * 100).roundToInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOverHalf) Color.White.copy(alpha = 0.9f) else MarineAqua
                            )
                            Text(
                                text = "$waterProgress ml",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isOverHalf) Color.White else ForestGreenDark
                            )
                            Text(
                                text = "/ 2.000 ml",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverHalf) Color.White.copy(alpha = 0.85f) else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Milestone Badges (500ml, 1000ml, 1500ml, 2000ml)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        milestones.forEach { m ->
                            val reached = waterProgress >= m
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (reached) SuccessGreen.copy(alpha = 0.14f) else MintLight)
                                    .border(
                                        1.dp,
                                        if (reached) SuccessGreen.copy(alpha = 0.4f) else CardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (reached) SuccessGreen else TextSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${m}ml",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reached) SuccessGreen else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Quick Capacity Shortcuts: +100ml, +250ml, +500ml
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickWaterCapacityButton(
                            amountMl = 100,
                            subtitle = "Ngụm nhỏ",
                            onClick = { viewModel.addWater(100) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickWaterCapacityButton(
                            amountMl = 250,
                            subtitle = "1 Cốc tiêu chuẩn",
                            onClick = { viewModel.addWater(250) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickWaterCapacityButton(
                            amountMl = 500,
                            subtitle = "1 Bình thể thao",
                            onClick = { viewModel.addWater(500) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Binary No Sugar Commitment Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (isNoSugarDone) SuccessGreen.copy(alpha = 0.5f) else CardBorder,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isNoSugarDone) SuccessGreen.copy(alpha = 0.15f) else MintLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoDrinks,
                                    contentDescription = "No Sugar",
                                    tint = if (isNoSugarDone) SuccessGreen else ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CAM KẾT TUYỆT ĐỐI: NO SUGAR",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Cắt đứt trà sữa, nước ngọt, bánh kẹo tinh luyện",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNoSugarDone) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFFEE2E2))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isNoSugarDone) "ĐẠT" else "CHƯA ĐẠT",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = if (isNoSugarDone) SuccessGreen else DangerCrimson
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.toggleHabit("no_sugar", isNoSugarDone, if (isNoSugarDone) 1 else 0, 1)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNoSugarDone) SuccessGreen else ForestGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isNoSugarDone) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNoSugarDone) "Đã Giữ Vững Kỷ Luật Không Đường Hôm Nay" else "Xác Nhận: Hôm Nay Không Nạp Đường",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 3. Food Group Carousel (Clean Eating, High Protein, Low Carb, Thực phẩm phục hồi cơ)
        item {
            Column {
                Text(
                    text = "DANH MỤC THỰC PHẨM KỶ LUẬT (CAROUSEL)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = ForestGreen
                )
                Text(
                    text = "Vuốt ngang để chọn chế độ dinh dưỡng phù hợp mục tiêu trong ngày",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(foodGroups, key = { it.id }) { group ->
                        val isSelected = group.id == selectedGroupId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) group.accentColor else CardWhite
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .width(175.dp)
                                .shadow(
                                    elevation = if (isSelected) 8.dp else 2.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = group.accentColor.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) group.accentColor else CardBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedGroupId = group.id }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.2f)
                                                else group.accentColor.copy(alpha = 0.14f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = group.icon,
                                            contentDescription = group.title,
                                            tint = if (isSelected) Color.White else group.accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        text = "${group.foods.size} món",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else group.accentColor,
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.2f) else MintLight,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = group.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (isSelected) Color.White else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = group.badge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Detailed Food Items for Selected Category
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MintSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MintBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = activeFoodGroup.icon,
                        contentDescription = null,
                        tint = activeFoodGroup.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${activeFoodGroup.title}: ${activeFoodGroup.subtitle}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenDark
                    )
                }
            }
        }

        items(activeFoodGroup.foods, key = { it.name }) { dish ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dish.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = dish.portion,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeFoodGroup.accentColor,
                            modifier = Modifier
                                .background(activeFoodGroup.accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dish.macros,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dish.benefit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickWaterCapacityButton(
    amountMl: Int,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MintLight)
            .border(1.dp, MarineAqua.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalDrink,
            contentDescription = "+$amountMl ml",
            tint = MarineAqua,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "+$amountMl ml",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = ForestGreenDark
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
