RUNNING THE PROJECT (HƯỚNG DẪN CHẠY ỨNG DỤNG)

Mục đích
- File này hướng dẫn người khác (developer / tester) build, chạy và kiểm thử ứng dụng Android "Book Browser" trên máy Windows.

Checklist (những việc sẽ hướng dẫn trong file này)
- [ ] Yêu cầu môi trường
- [ ] Cách build APK trên Windows (cmd.exe)
- [ ] Cách cài và chạy trên thiết bị/emulator
- [ ] Cách kiểm thử trạng thái lỗi (network fail + DB empty)
- [ ] Cách chạy unit test và instrumentation test
- [ ] Các mẹo debug (logcat, kiểm tra DB, strings)

1) YÊU CẦU MÔI TRƯỜNG
- Windows 10/11 (hoặc tương đương)
- Android Studio (phiên bản gần đây)
- JDK 11+
- Android SDK (phiên bản phù hợp project - dùng Android Studio để cài)
- Thiết bị Android hoặc Android Emulator
- ADB trong PATH (cài cùng Android SDK Platform Tools)

2) MỞ PROJECT
- Mở Android Studio và chọn `Open` → chỉ đến thư mục project: `NguyenDuyHungSE184681` (thư mục gốc chứa file `build.gradle.kts` và thư mục `app`).

3) BUILD TỪ COMMAND LINE (cmd.exe)
- Mở `cmd.exe` ở thư mục project (gốc).
- Build debug APK:

```bat
cd C:\Users\Admin\AndroidStudioProjects\NguyenDuyHungSE184681
.\gradlew.bat assembleDebug
```

- Cài APK lên thiết bị đã kết nối (adb phải thấy thiết bị):

```bat
.\gradlew.bat installDebug
```

4) CHẠY ỨNG DỤNG TRONG ANDROID STUDIO
- Hoặc nhấn Run (Shift+F10) trong Android Studio, chọn device/emulator.

5) KIỂM THỬ TRẠNG THÁI LỖI (Error State)
Yêu cầu: "Khi network fetch bị fail và database cục bộ trống, UI phải hiển thị thông báo thân thiện: 'Could not load data. Please check your connection.' (hoặc string resource tương đương)."

Cách kiểm thử thủ công (khuyến nghị):

- Bước A: Đảm bảo database app rỗng (fresh install hoặc xóa dữ liệu app):
  - Trên emulator/device: Settings → Apps → chọn app → Storage → Clear data
  - Hoặc gỡ và cài lại APK (`adb uninstall <package>` rồi `installDebug`).

- Bước B: Mô phỏng mất mạng:
  - Tắt Wi-Fi và dữ liệu trên device/emulator
  - Hoặc dùng tính năng "Cellular"/Network -> Turn Off

- Bước C: Chạy app (khi DB rỗng và không có mạng):
  - App sẽ cố fetch từ API, thất bại, và kiểm tra DB (rỗng) → nên hiển thị error message
  - Trên màn hình chính (MainActivity) bạn sẽ thấy `TextView` lỗi hiển thị thông báo thân thiện.

- Ghi chú: nội dung thông báo lưu trong `res/values/strings.xml` (key: `error_loading` hoặc literal). Bạn có thể thay đổi/đối chiếu.

Kiểm thử tự động (instrumentation)
- Bạn có thể viết một fake repository (bằng Hilt test binding hoặc ServiceLocator) để:
  - Trả về cache rỗng
  - Khi fetch remote thì ném IOException
- Sau đó chạy instrumentation test (Espresso) để xác nhận `R.id.error_text` (hoặc id tương ứng) hiển thị thông báo mong muốn.

6) COMMANDS CHẠY TESTS
- Unit tests (JVM):

```bat
.\gradlew.bat test
```

- Instrumentation tests (connected device/emulator):

```bat
.\gradlew.bat connectedAndroidTest
```

7) DEBUG (logcat, kiểm tra DB, kiểm tra LiveData)
- Xem logcat (chỉ output liên quan đến tag MainActivity):

```bat
adb logcat | findstr MainActivity
```

- Nếu bạn cần toàn bộ log (Windows PowerShell):

```powershell
adb logcat -v time > logcat.txt
```

- Kiểm tra DB (Room)
  - Bạn có thể dùng `adb shell` để pull database file từ máy ảo:

```bat
adb shell
run-as com.example.nguyenduyhung_se184681
cd /data/data/com.example.nguyenduyhung_se184681/databases
ls -l
exit
```

  - Sau đó `adb pull` file DB ra máy để inspect bằng SQLite Browser.

8) Kiểm tra text hiển thị lỗi trong mã
- Thông báo lỗi thân thiện dùng string resource `error_loading` trong `res/values/strings.xml`.
- Nếu test hiển thị không đúng, kiểm tra trong `MainActivity.java` phần xử lý lỗi (showError(...) nơi gọi message).

9) Các IDs, resource hữu ích
- `R.id/error_text` - TextView hiển thị thông báo lỗi (MainActivity)
- `res/values/strings.xml` - chứa các string hiển thị cho user (ví dụ: `error_loading`, `no_favorites`)

10) Mẹo nhanh khi gặp vấn đề phổ biến
- App luôn hiển thị dữ liệu cũ sau khi unfavorite nhiều mục:
  - Xoá data app và thử lại
  - Kiểm tra LiveData observer (chỉ observe 1 lần)
- Ảnh không hiển thị hoặc mờ:
  - Kiểm tra URL ảnh (https) và Glide logs
  - Kiểm tra mạng
- Category chips không update:
  - Check `selectedCategories` giá trị và restore logic trong `onResume` / `restoreCategoryChipStates()`

11) Cách mô phỏng API thật (Google Books)
- Project hiện đã tích hợp Google Books API trong `PostRepository.fetchPostsFromApi()` (đã convert response to `Post` model). Bạn có thể thay query hoặc số lượng item trong file `PostRepository.java`.

12) Liên hệ / Ghi chú
- File README chính của project nằm ở gốc repo; file này tập trung vào chạy và test.
- Nếu cần, tôi có thể bổ sung script gradle tasks hoặc sample fakes để test error state tự động.

---
