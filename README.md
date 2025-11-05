# Book Browser (Android)

A simple Android app to browse books, mark favorites, and filter by category. This project uses Java/Kotlin, Retrofit for network calls, Room for local caching, and a ViewModel for UI state.

## Key Features
- Browse books from a remote API
- Local caching with Room
- Favorite/unfavorite books
- Category chips to filter displayed books
- Friendly error state when network fails and cache is empty

## Project Structure
- `app/src/main/java/com/example/nguyenduyhung_se184681/`
  - `MainActivity.java`, `DetailActivity.java`, `FavoritesActivity.java`
  - `adapter/` - UI adapters (e.g. `PostAdapter.java`)
  - `api/` - network client and service (`ApiService.java`, `RetrofitClient.java`)
  - `database/` - Room database and DAOs (`AppDatabase.java`, `PostDao.java`)
  - `repository/` - data repository (`PostRepository.java`)
  - `viewmodel/` - ViewModel classes (`PostViewModel.java`)
  - `model/` - data models (`Post.java`, `GoogleBooksResponse.java`)
- `app/src/main/res/` - layouts, drawables, strings and themes
- Tests: `app/src/test/` and `app/src/androidTest/`

> Note: UI strings visible to users were updated from "post/posts" to "book/books" (internal class names like `PostRepository` were not renamed).

## Requirements
- Windows (development tested)
- Android Studio Otter | 2025.2.1
- JDK 11+ (project configured Gradle wrapper)
- Android SDK (matching project `compileSdk` and `targetSdk`)

## Build & Run (Windows)
1. Open project in Android Studio.
2. Build and run on an emulator or device.
3. Or from command line (project root):
   - Build: `.\gradlew.bat assembleDebug`
   - Install on connected device: `.\gradlew.bat installDebug`

## Run Tests
- Unit tests:
  - `.\gradlew.bat test`
- Instrumented (connected) tests:
  - `.\gradlew.bat connectedAndroidTest`

## Error / Offline Behavior
The app shows a user-friendly error message (for example: "Could not load data. Please check your connection.") when a network fetch fails and the local database has no cached books. To test this behavior:
- Simulate network failure and ensure local DB empty.
- The UI should display the error `TextView` with the friendly message.

## How to Contribute
- Open an issue for bugs or feature requests.
- Create a branch named `feature/your-change` and open a pull request.
- Keep UI text in `res/values/strings.xml` for easy localization.

## Known Notes & TODOs
- Internal names still include `Post` (e.g., `PostRepository`, `PostAdapter`). These do not affect user-visible text.
- Consider migrating internal names and DB tables if a full rename is required (requires migration plan).
- Unit and UI tests for error state and category persistence are recommended (ViewModel mocks and Espresso tests).

## License
Add your preferred license file (e.g., `LICENSE`) at project root.

## Contact
Project owner: `userduyhung` (GitHub)
