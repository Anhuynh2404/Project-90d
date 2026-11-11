# ==============================================================================
# PROJECT 90 (v2.0) - PRODUCTION PROGUARD / R8 RULES
# Kiến trúc: MVVM + Room Database (KSP) + Kotlin Coroutines/Flow + Jetpack Compose
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. BẢO TOÀN METADATA KOTLIN, GENERICS & STACKTRACE CHO BÁO CÁO CRASH
# ------------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Giữ số dòng (LineNumberTable) để đọc được log lỗi khi crash trên Production
# nhưng ẩn tên file gốc thành "SourceFile" nhằm bảo mật cấu trúc mã nguồn
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Giữ metadata của Kotlin để Reflection / Coroutines / Compose hoạt động chính xác
-keep class kotlin.Metadata { *; }

# ------------------------------------------------------------------------------
# 2. ROOM DATABASE (ENTITIES, DAOs, DATABASE & KSP GENERATED _Impl)
# ------------------------------------------------------------------------------
# Giữ toàn bộ các bảng dữ liệu (@Entity) không bị đổi tên trường (column mapping)
# Bao gồm: UserEntity, HabitEntity, DailyLogEntity, JournalEntity,
# FinanceTransactionEntity, DebtEntity, SavingsGoalEntity, TimeAllocationEntity
-keep @androidx.room.Entity class * { *; }
-keep class com.example.data.local.entity.** { *; }

# Giữ toàn bộ các Enum định nghĩa danh mục trong Room & UI
-keepclassmembers enum com.example.data.local.entity.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Giữ các interface DAO và các lớp _Impl do Room KSP sinh tự động
-keep @androidx.room.Dao class * { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    <init>();
    *;
}
-keep class com.example.data.local.database.** { *; }
-keep class **_Impl { *; }

# Tránh cảnh báo từ SQLite / Room Cursor
-dontwarn androidx.room.paging.**
-dontwarn androidx.sqlite.**

# ------------------------------------------------------------------------------
# 3. KIẾN TRÚC MVVM (VIEWMODEL, REPOSITORY & UI STATE MODELS)
# ------------------------------------------------------------------------------
# Giữ các constructor của AndroidViewModel / ViewModel để ViewModelProvider
# khởi tạo thành công qua Reflection ở chế độ Release
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
    <init>(android.app.Application);
    <init>(android.app.Application, ...);
    <init>(androidx.lifecycle.SavedStateHandle);
}

# Giữ Project90ViewModel và các Data Class / Enum biểu diễn StateFlow trên UI
# (DashboardSummary, PomodoroUiState, PomodoroMode, AppThemePreset, AppBackgroundPreset, DynamicThemeState)
-keep class com.example.ui.viewmodel.** { *; }
-keep class com.example.data.repository.HabitRepository { *; }

# ------------------------------------------------------------------------------
# 4. HỆ THỐNG ANDROID SERVICES & BROADCAST RECEIVERS (CHẠY NGẦM 24/7)
# ------------------------------------------------------------------------------
# Giữ Foreground Service đếm bước chân (Pedometer Sensor.TYPE_STEP_COUNTER)
# và NotificationListenerService đọc biến động số dư ngân hàng
-keep class com.example.service.PersistentHabitNotificationService { *; }
-keep class com.example.service.BankNotificationListenerService { *; }

# Giữ các BroadcastReceiver khởi động cùng thiết bị và nhắc nhở định kỳ
-keep class com.example.service.BootCompletedReceiver { *; }
-keep class com.example.service.DailyReminderReceiver { *; }
-keep class com.example.service.QuickLogActionReceiver { *; }

# ------------------------------------------------------------------------------
# 5. KOTLIN COROUTINES & STATEFLOW / SHAREDFLOW
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Bảo toàn các trường volatile được AtomicReferenceFieldUpdater truy cập trong Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-dontwarn kotlinx.coroutines.flow.**

# ------------------------------------------------------------------------------
# 6. JETPACK COMPOSE & MATERIAL 3 STABILITY
# ------------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ------------------------------------------------------------------------------
# 7. BẢO MẬT & TỐI ƯU HÓA LOG TRÊN BẢN PRODUCTION (RELEASE)
# ------------------------------------------------------------------------------
# Tự động loại bỏ các lệnh Log.v, Log.d, Log.i khỏi bytecode khi xuất APK/AAB Release
# giúp giảm kích thước file và tránh lộ thông tin nhạy cảm qua Logcat
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
