# LearnHub – Learning Dashboard

Android app (Kotlin, XML Views): log in, view courses and progress, mark lessons completed, and keep using previously loaded courses offline.
**Run:** open in Android Studio, or `./gradlew assembleDebug`. Demo login (mocked): `demo@learnhub.com` / `Learn@123`. **Tests:** `./gradlew testDebugUnitTest`.

## 1. Architecture
**MVVM + Repository**: Fragment (XML) → ViewModel (`StateFlow`) → Repository → Room / mock API.
- Fragments only render a sealed `UiState` (Loading / Success / Empty / Error) and forward clicks, so rotation and error states are handled in one place.
- Business rules (validation, progress calculation, which state to show) live in ViewModels, the Repository and `ProgressCalculator`, so they are unit-tested on the JVM with fakes.
- The remote source is an interface, so a Retrofit API can replace the mock without touching the UI.

## 2. Offline Support
**Room is the single source of truth**: screens read only from the database and the network only refreshes it.
- Courses and lessons (with their completed flag) are stored in Room. Completing a lesson updates the lesson and recalculates progress in one transaction; completing it again does nothing, so progress can't double count.
- On refresh, remote data is merged into Room and **local completions are kept**. If the refresh fails, the cached courses are shown; with no cache, the user sees an error and a Retry button.
- Data survives app restarts, and the dashboard and details screens update automatically because both observe Room.

## 3. Security
Store the auth token in **`EncryptedSharedPreferences` backed by the Android Keystore** (implemented in `EncryptedTokenStore`), or DataStore with a Keystore-held key, since Jetpack Security is deprecated. Use short-lived access tokens with a refresh token, exclude the token file from backups, never log credentials or tokens, and use HTTPS only (cleartext is disabled).

## 4. Scale (1M users, hundreds of courses)
1. **Pagination:** paged course lists (Paging 3 + `RemoteMediator`); load lessons only when a course is opened.
2. **Server-side progress + sync:** store completions on the backend and sync with WorkManager (retry, batching, conflict rules).
3. **Caching/CDN:** CDN for course content, HTTP caching/ETags, and a Redis cache for hot endpoints.
4. **Backend:** stateless services behind a load balancer, read replicas, indexed queries, rate limiting.
5. **Observability:** crash/performance monitoring, feature flags and staged rollouts.

## 5. Second Platform (iOS/macOS)
Keep the same architecture with SwiftUI: `View` renders state from an `@Observable` view model, a repository coordinates **SwiftData** (Room equivalent) and `URLSession` (Retrofit equivalent) using `async/await`, navigation uses `NavigationStack`, and the token goes in the **Keychain**. The UI-state enum, the progress formula and the "cache first, never lose local completion" rules carry over unchanged.
