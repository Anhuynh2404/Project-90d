# 🌿 Project 90 (v2.0.0 — Eco-Botanical Central Command)

> **"Kỷ luật là cầu nối giữa mục tiêu và thành tựu."**  
> *Project 90 (Phiên bản 2.0)* là ứng dụng Bảng Điều Khiển Kỷ Luật 90 Ngày (Central Command Habit & Discipline Tracker) được xây dựng trên nền tảng **Android Hiện Đại (Kotlin & Jetpack Compose)** với ngôn ngữ thiết kế **Eco-Botanical & Glassmorphism**, giúp bạn làm chủ vận động, dinh dưỡng, thời gian và tài chính trước tháng Hai.

---

## ✨ Điểm Mới Trong Phiên Bản v2.0.0

### 1. 🎛️ Bảng Điều Khiển Trung Tâm (Central Command Dashboard 2.0)
- **Giao diện Glassmorphism & Soft Shadows:** Thẻ trung tâm hiệu ứng kính mờ đa lớp, bóng đổ sinh thái mềm mại và ánh sáng khuếch tán tự nhiên.
- **Vòng Tròn Đếm Ngược 90 Ngày Động (Concentric Animated Rings):**
  - Vòng ngoài hiển thị hành trình 90 ngày kèm điểm sáng dẫn đường và hiệu ứng nhịp thở sinh thái (Breathing Aura).
  - Vòng trong hiển thị **% hoàn thành mục tiêu trong ngày** theo thời gian thực với chuyển động mượt mà (`Spring` & `FastOutSlowInEasing`).
- **Hệ Thống 4 Widget/Card Bo Góc Tóm Tắt Chỉ Số Ngày:**
  - 💧 **Nước & No Sugar:** Hiển thị lượng nước đã uống (`/ 2.000 ml`), trạng thái No Sugar, thanh tiến độ và **% hoàn thành trong ngày**, kèm nút nạp nhanh `+250 ml`.
  - 🏃 **Bước Chân & Vận Động:** Tích hợp cảm biến `TYPE_STEP_COUNTER` (`/ 10.000 bước`), tiến độ 3 bài tập thể lực (Hít đất, Squat, Tắm lạnh), **% hoàn thành trong ngày**, kèm nút cộng nhanh `+500 bước`.
  - ⏱️ **Thời Gian & Detox:** Tổng hợp thời lượng đọc sách tập trung (`/ 20 phút`), nhật ký 3 dòng tối và 3 mốc giới nghiêm Digital Detox (`14:00 Cafe`, `22:00 Màn hình`, `22:30 Ngủ`) cùng **% hoàn thành trong ngày**.
  - 💰 **Tài Chính 10%:** Tóm tắt Quỹ Tiết Kiệm Bất Khả Xâm Phạm 10%, trạng thái kỷ luật tài chính và **% hoàn thành trong ngày**.

### 2. 🧹 Kiến Trúc Tinh Gọn & Tối Ưu Dung Lượng
- Loại bỏ hoàn toàn module *Báo thức Hardcore 5AM* cùng các thư viện nặng không còn sử dụng (`CameraX`, `Retrofit`, `OkHttp`, `Moshi`), giúp giảm đáng kể dung lượng APK, tăng tốc độ khởi động và tiết kiệm pin tối đa.

### 3. 📌 Thanh Thông Báo Đẩy Bám Sát 24/7 (Full-Width Ongoing Dashboard)
- Ghim cố định 24/7 trên thanh trạng thái (`START_STICKY`, `FLAG_ONGOING_EVENT`, `FLAG_NO_CLEAR`), hiển thị trực quan tiến độ ngày và nút thao tác nhanh (`+250ml nước`, `+500 bước`).

---

## 🏗️ Kiến Trúc Kỹ Thuật (Architecture & Tech Stack)

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── dao/             # Room DAOs (User, Habit, DailyLog, Finance)
│   │   ├── database/        # Room Database (Project90Database)
│   │   └── entity/          # Room Entities
│   └── repository/          # Repository Pattern quản lý dữ liệu tập trung
├── sensor/                  # Cảm biến đếm bước chân phần cứng (Pedometer)
├── service/
│   ├── PersistentHabitNotificationService.kt  # Foreground Service 24/7
│   ├── HabitNotificationHelper.kt             # Tạo & Cập nhật Dashboard Notification
│   ├── BankNotificationListenerService.kt     # Tự động bóc tách biến động số dư ngân hàng
│   ├── NotificationActionReceiver.kt          # Xử lý nút bấm nhanh trên thông báo
│   └── BootReceiver.kt                        # Khôi phục dịch vụ khi khởi động lại máy
├── ui/
│   ├── components/          # Vòng tròn kép 90 ngày v2.0, Thẻ thói quen, BottomNav
│   ├── screens/             # Dashboard 2.0 & các màn hình mô-đun chuyên sâu
│   ├── theme/               # Theme Eco-Botanical, M3 ColorScheme, Typography
│   └── viewmodel/           # Project90ViewModel (StateFlow & Coroutines)
└── MainActivity.kt          # Entry Point & Điều hướng Navigation Compose
```

---

## 🚀 Hướng Dẫn Build & Xuất File APK

```bash
gradle assembleDebug
```
File APK hoàn chỉnh được xuất tại:
- `app/build/outputs/apk/debug/app-debug.apk`
- `public/Project90-v2.0.0.apk`
