# Dynamic Wallpaper - Complete Rewrite TODO

## Terminology
- **Playlist** = A wallpaper changing plan with rules, triggers, and actions (formerly "Collection")
- **Pack** = A group of purchasable/downloadable images (wallpaper pack)
- **Subcollection** = A named subset of images within a Playlist

---

## Phase 1: Project Setup & Foundation
> Set up the Kotlin Multiplatform project structure using ls-kiteui-starter as the template.

- [ ] Create new KMP project structure with modules: `apps/`, `server/`, `shared/`
- [ ] Configure `settings.gradle.kts` with all three modules
- [ ] Configure `build.gradle.kts` root build file with KiteUI, Lightning Server, KSP plugins
- [ ] Configure `gradle/libs.versions.toml` version catalog (Kotlin 2.2.20, KiteUI 7.x, LS 5.x)
- [ ] Configure `apps/build.gradle.kts` for multiplatform targets (Android, iOS, JS/Web, Desktop)
- [ ] Configure `server/build.gradle.kts` for JVM backend
- [ ] Configure `shared/build.gradle.kts` for shared models
- [ ] Set up Android module with `MainActivity.kt` extending `KiteUiActivity`
- [ ] Set up iOS module with `App.ios.kt` entry point
- [ ] Set up Web/JS module with `Simple.kt` entry point
- [ ] Set up basic `App.kt` with bottom navigation (4 tabs)
- [ ] Set up `@Routable` pages for each tab: Playlists, My Images, Explore, Profile
- [ ] Configure initial KiteUI theme using `Theme.flat2()` or `Theme.m3()`
- [ ] Set up `appTheme` as a reactive `Signal<Theme>` for dynamic theming
- [ ] Verify app builds and runs on Android, Web, and iOS with empty tab screens
- [ ] Set up git repository for new project

---

## Phase 2: Shared Models & Data Classes
> Define all shared data models in the `shared/` module.

- [ ] Define `User` model (id, email, name, role, createdAt)
- [ ] Define `Playlist` model (id, name, ownerId, photoFileNames, selectedImageIndex, subcollections, rules, isPublic, price, downloadCount)
- [ ] Define `SubPlaylist` model (name, fileNames - subset of parent playlist images)
- [ ] Define `Rule` model (id, trigger, action)
- [ ] Define `Trigger` sealed class/interface with subtypes:
  - [ ] `TriggerByTimeInterval` (intervalAmount, intervalType, timeToTrigger, dayOfWeek, isExact)
  - [ ] `TriggerByDate` (month, day)
  - [ ] `TriggerBySeason` (seasons list with start/end dates)
  - [ ] `TriggerByLocation` (latitude, longitude, radius, enterOrExit)
  - [ ] `TriggerByWeather` (condition type, temperature values, location method, updateInterval)
- [ ] Define `Action` sealed class/interface with subtypes:
  - [ ] `NextInPlaylist`
  - [ ] `PreviousInPlaylist`
  - [ ] `RandomInPlaylist`
  - [ ] `SwitchToSubPlaylist(subPlaylistName)`
  - [ ] `SpecificWallpaper(imageFileName)`
- [ ] Define `Pack` model (id, name, description, imageFileNames, previewImages, price, creatorId, downloadCount, tags, isFeatured)
- [ ] Define `Season` model (name, startMonth, startDay, endMonth, endDay)
- [ ] Define `WeatherCondition` enum (Clear, MostlyClear, Clouds, Rain, Snow, Thunderstorm, Fog, Windy, etc.)
- [ ] Define `IntervalType` enum (Minutes, Hour, Day, Week, Month)
- [ ] Define `AppPlatform` enum (Android, iOS, Web, Desktop)
- [ ] Define `UserRole` enum (User, Creator, Admin)
- [ ] Add `@Serializable`, `@GenerateDataClassPaths`, and `HasId<Uuid>` to all models
- [ ] Verify shared module compiles for all targets

---

## Phase 3: Server Setup
> Set up the Lightning Server backend with endpoints and authentication.

- [ ] Create `Server.kt` with `ServerBuilder` pattern (database, cache, email, files settings)
- [ ] Create `Main.kt` with CLI entry point (serve/sdk commands)
- [ ] Set up `UserAuth` with email-based authentication (PinHandler + EmailProofEndpoints)
- [ ] Create `UserEndpoints` with ModelRestEndpoints for User CRUD
- [ ] Create `PlaylistEndpoints` with ModelRestEndpoints for Playlist CRUD
- [ ] Create `PackEndpoints` with ModelRestEndpoints for Pack CRUD
- [ ] Set up file upload endpoint (`UploadEarlyEndpoint`) for wallpaper images
- [ ] Configure `PublicFileSystem` for image storage (S3 or local)
- [ ] Set up `MetaEndpoints` for health checks and bulk API
- [ ] Configure CORS settings for web client
- [ ] Add model permissions (users can only edit own playlists/packs, admins can edit all)
- [ ] Add lifecycle hooks for Playlist (interceptCreate for validation, postChange for notifications)
- [ ] Generate initial SDK with `./gradlew :server:generateSdk`
- [ ] Verify server starts and health endpoint responds
- [ ] Test authentication flow (register, login, logout)

---

## Phase 4: Client SDK & API Layer
> Set up the client-side API integration and offline-first data layer.

- [ ] Copy generated SDK files (`Api.kt`, `LiveApi.kt`, `CachedApi.kt`) to `apps/src/commonMain/`
- [ ] Set up `ApiOption` enum with server URLs (SameServer, Local, Production)
- [ ] Set up `selectedApi` persistent property
- [ ] Set up `sessionToken` persistent property
- [ ] Create `UserSession` class extending `CachedApi`
- [ ] Set up `currentSession` reactive computed property
- [ ] Implement JSON-based local storage layer:
  - [ ] Define `expect/actual` for `getFileByteArray(fileName)` and `saveFile(byteArray, fileName)`
  - [ ] Android actual: read/write from `filesDir`
  - [ ] Web actual: IndexedDB-based storage
  - [ ] iOS actual: NSFileManager-based storage
  - [ ] Desktop actual: file system storage
- [ ] Implement `ModelOfflineSyncStoreApi` for offline-first data access pattern
- [ ] Implement image storage layer:
  - [ ] Define `expect/actual` for `saveImageToStorage()`, `readImageFromStorage()`, `deleteImageFromStorage()`
  - [ ] Android actual: internal storage with FileInputStream
  - [ ] Web actual: IndexedDB blob storage
  - [ ] iOS actual: file-based storage
- [ ] Verify API calls work end-to-end (create user, fetch data)

---

## Phase 5: Navigation & App Shell
> Build the main app shell with bottom navigation and page routing.

- [ ] Implement bottom navigation bar with 4 tabs:
  - [ ] Playlists tab (icon: playlist/queue icon)
  - [ ] My Images tab (icon: image/photo icon)
  - [ ] Explore tab (icon: compass/search icon)
  - [ ] Profile/Settings tab (icon: person/settings icon)
- [ ] Create page stubs with `@Routable` annotations:
  - [ ] `PlaylistsPage` (`/playlists`) - main playlists list
  - [ ] `CreatePlaylistPage` (`/playlists/create`) - new playlist
  - [ ] `EditPlaylistPage` (`/playlists/{id}/edit`) - edit existing
  - [ ] `PlaylistDetailPage` (`/playlists/{id}`) - view playlist details
  - [ ] `SelectTriggersPage` (`/playlists/{id}/triggers`) - trigger selection
  - [ ] `ConfigureTriggerPage` (`/playlists/{id}/triggers/{type}`) - trigger config
  - [ ] `SelectActionsPage` (`/playlists/{id}/actions`) - action selection
  - [ ] `ViewImagePage` (`/images/{id}`) - full-screen image view
  - [ ] `ReorderImagesPage` (`/playlists/{id}/reorder`) - drag-and-drop reorder
  - [ ] `MyImagesPage` (`/my-images`) - image library
  - [ ] `ExplorePage` (`/explore`) - explore/marketplace
  - [ ] `PackDetailPage` (`/explore/packs/{id}`) - pack detail/preview
  - [ ] `ExplorePlaylistDetailPage` (`/explore/playlists/{id}`) - shared playlist detail
  - [ ] `ProfilePage` (`/profile`) - profile/settings
  - [ ] `LoginPage` (`/login`) - authentication
  - [ ] `CreateAccountPage` (`/create-account`) - account creation prompt
- [ ] Set up PageNavigator for main content and dialog navigator
- [ ] Implement tab switching logic with page navigator reset
- [ ] Verify navigation works across all pages

---

## Phase 6: Playlists Tab - Core Features
> Implement the main playlist management functionality.

### Playlist List Screen
- [ ] Display list of user's playlists using `recyclerView` or `forEach`
- [ ] Show playlist name, image count, preview thumbnails
- [ ] Show currently active playlist indicator
- [ ] "Create New Playlist" FAB/button
- [ ] Empty state with helpful message when no playlists exist
- [ ] Tap playlist to navigate to detail/edit
- [ ] Long-press or menu for delete with confirmation dialog

### Create/Edit Playlist Screen
- [ ] Playlist name input field with validation
- [ ] "Select Images" button to open image picker
- [ ] Image preview grid (show up to 5 random images from playlist)
- [ ] Image count display
- [ ] "View/Reorder Images" button -> ReorderImagesPage
- [ ] Rules list with RecyclerView showing trigger+action descriptions
- [ ] "Add Rule" button -> SelectTriggersPage
- [ ] Delete individual rules
- [ ] Subcollection toggle checkbox
- [ ] "Add Subcollection" button (visible when toggle enabled)
- [ ] Subcollection list display
- [ ] "Save" button (persists to local JSON + syncs to server if logged in)
- [ ] "Cancel" button (discards changes)
- [ ] "Delete" button (with confirmation, for editing existing playlists)

### Image Selection & Cropping
- [ ] Platform-specific image picker (expect/actual):
  - [ ] Android: `ACTION_GET_CONTENT` with `EXTRA_ALLOW_MULTIPLE`
  - [ ] iOS: `PHPickerViewController`
  - [ ] Web: `<input type="file" multiple accept="image/*">`
  - [ ] Desktop: file chooser dialog
- [ ] Image cropping with screen aspect ratio:
  - [ ] Calculate device screen resolution and aspect ratio
  - [ ] Crop images to match screen dimensions
  - [ ] Platform-specific cropping library or built-in cropping UI
- [ ] Save cropped images to local storage under playlist directory
- [ ] Track image filenames in playlist model

### Photo Order Management
- [ ] Grid view (3 columns) of all images in playlist
- [ ] Drag-and-drop reordering (platform-dependent implementation)
- [ ] Tap image to view full-screen (ViewImagePage)
- [ ] Delete individual images from full-screen view with confirmation
- [ ] Save reordered list

### Subcollections (Sub-Playlists)
- [ ] Create subcollection screen: name input + image selection from parent playlist
- [ ] Grid view of parent images with selection checkboxes
- [ ] Edit existing subcollection
- [ ] Delete subcollection
- [ ] Display subcollections in playlist editor

---

## Phase 7: Rules System (Triggers + Actions)
> Implement the trigger/action rule system for automated wallpaper changes.

### Trigger Selection Screen
- [ ] Radio button list of trigger types:
  - [ ] Time Interval
  - [ ] Specific Date
  - [ ] By Season
  - [ ] By Location (Android/Desktop only indicator)
  - [ ] By Weather
  - [ ] By Calendar Event (future - disabled/greyed out)
- [ ] "Next" button to navigate to trigger configuration

### Time Interval Trigger Configuration
- [ ] Repeat interval amount input (numeric)
- [ ] Interval type selector (Minutes, Hour, Day, Week, Month)
- [ ] Time of day picker
- [ ] Day of week chip selector (for weekly intervals, show/hide based on type)
- [ ] Exact timing toggle
- [ ] Validation before proceeding

### Date Trigger Configuration
- [ ] Date picker (month and day)
- [ ] Display selected date
- [ ] Validation (ensure date is selected)

### Season Trigger Configuration
- [ ] 4 season sections (Winter, Spring, Summer, Fall)
- [ ] Checkbox to enable/disable each season
- [ ] Date picker for start and end date of each enabled season
- [ ] Display selected date ranges
- [ ] Validation

### Location Trigger Configuration (Platform-Specific)
- [ ] Map display (platform-specific map implementation)
  - [ ] Android: osmdroid or Google Maps
  - [ ] Web: Leaflet.js or embedded OSM
  - [ ] iOS: MapKit
  - [ ] Desktop: embedded web map or osmdroid
- [ ] "Use My Location" button with GPS
- [ ] Visual radius indicator
- [ ] Enter/Exit radio toggle
- [ ] Location permission handling per platform

### Weather Trigger Configuration
- [ ] Temperature condition radio group:
  - [ ] When temperature is (exact value)
  - [ ] When temperature is less than
  - [ ] When temperature is greater than
  - [ ] When temperature is between (range)
  - [ ] When weather condition is (dropdown: Clear, Rain, Snow, etc.)
- [ ] Location method selector (IP, GPS, manual coordinates)
- [ ] Forecast update interval selector
- [ ] Validation

### Action Selection Screen
- [ ] Radio button list of action types:
  - [ ] Next in Playlist
  - [ ] Random in Playlist/Sub-Playlist
  - [ ] Switch to Sub-Playlist (with bottom sheet selector)
  - [ ] Specific Wallpaper (with bottom sheet image grid selector)
- [ ] "Save Rule" button -> returns to playlist editor with new rule

---

## Phase 8: Wallpaper Engine
> Implement the cross-platform wallpaper setting and automation engine.

### Wallpaper Setting (expect/actual per platform)
- [ ] Android: `WallpaperManager.setBitmap()` for home screen
- [ ] Desktop: platform-specific wallpaper API (Windows/macOS/Linux)
- [ ] iOS: Save to photos library with prompt (no programmatic wallpaper setting)
- [ ] Web: CSS background change (for in-app preview only)

### Playlist Navigation Engine (shared/common)
- [ ] `goToNextWallpaper()` - advance index, wrap around at end
- [ ] `goToPreviousWallpaper()` - decrement index, wrap to last at beginning (fix bug from original)
- [ ] `goToRandomWallpaper()` - random index selection
- [ ] `goToSpecificWallpaper(fileName)` - set by filename
- [ ] `switchToSubPlaylist(name)` - change active sub-playlist and advance
- [ ] `runAction(ruleIndex)` - dispatch action from rule
- [ ] Subcollection-aware navigation (use sub-playlist images when one is active)

### Trigger Scheduling (platform-specific)
- [ ] Android:
  - [ ] AlarmManager for time-based triggers (exact and inexact)
  - [ ] GeofencingClient for location triggers
  - [ ] Boot receiver to restore triggers after reboot
  - [ ] Quick Settings tiles (Next, Previous, Shuffle)
- [ ] Desktop:
  - [ ] System scheduler / timer for time-based triggers
  - [ ] GPS/location service integration
- [ ] iOS:
  - [ ] Background fetch for periodic checks
  - [ ] Significant location change monitoring
  - [ ] Notification-based reminders (since can't set wallpaper programmatically)
- [ ] Web:
  - [ ] Service Worker for background scheduling
  - [ ] Geolocation API for location triggers
  - [ ] Notification API for reminders

### Weather API Integration (shared/common)
- [ ] NWS API client for US weather data (`api.weather.gov`)
- [ ] IP-based geolocation (`ipapi.co` or similar)
- [ ] GPS location retrieval (expect/actual per platform)
- [ ] Forecast fetching and parsing
- [ ] Weather condition matching logic
- [ ] Predictive alarm scheduling based on forecast

### Android-Specific Services
- [ ] `GoToNextWallpaperTileService` (Quick Settings)
- [ ] `GoToPreviousWallpaperTileService` (Quick Settings)
- [ ] `ShuffleWallpaperTileService` (Quick Settings)
- [ ] `BootCompleteReceiver` for trigger restoration
- [ ] `AlarmActionReceiver` for alarm-based triggers
- [ ] `GeofenceBroadcastReceiver` for location triggers
- [ ] Notification channel setup

---

## Phase 9: Dynamic Theming (Color Extraction)
> Replace Android Material You with cross-platform color extraction from wallpaper.

- [ ] Implement color extraction from current wallpaper image:
  - [ ] Load current wallpaper bitmap/image data
  - [ ] Extract dominant colors (implement palette extraction algorithm or use library)
  - [ ] Determine primary, secondary, tertiary, and surface colors
  - [ ] Generate light and dark color variants
- [ ] Create KiteUI `Theme` from extracted colors:
  - [ ] Map extracted colors to KiteUI theme properties
  - [ ] Update `appTheme` Signal with new theme
  - [ ] Support both light and dark mode based on system preference
- [ ] Platform-specific color extraction:
  - [ ] Android: Use Palette API or manual extraction from Bitmap
  - [ ] iOS: Core Image / manual pixel sampling
  - [ ] Web: Canvas-based pixel sampling
  - [ ] Desktop: BufferedImage pixel sampling
- [ ] Trigger theme update when wallpaper changes
- [ ] Persist extracted theme colors locally for instant app start
- [ ] Fallback to default theme when no wallpaper is set

---

## Phase 10: My Images Tab
> Image library management for the user's wallpaper collection.

- [ ] Display all locally stored images in a grid
- [ ] Group images by playlist or show all
- [ ] Import images from device gallery
- [ ] Delete images (with confirmation, warn if used in playlists)
- [ ] Image preview on tap (full-screen view)
- [ ] Image metadata display (dimensions, file size, which playlists use it)
- [ ] Search/filter images
- [ ] Bulk selection mode for delete/export

---

## Phase 11: Explore Tab
> Marketplace for discovering and downloading packs and playlists.

### Explore Main Screen
- [ ] Featured section at top (horizontal scrolling carousel)
  - [ ] Featured packs and playlists curated by admin
  - [ ] Large preview cards with images
- [ ] Packs section
  - [ ] Horizontal scrolling row of pack cards
  - [ ] "See All" button to full packs list
  - [ ] Pack card: preview images, name, image count, price/free badge
- [ ] Playlists section
  - [ ] Horizontal scrolling row of playlist cards
  - [ ] "See All" button to full playlists list
  - [ ] Playlist card: preview images, name, rule count, trigger types
- [ ] Search bar at top for searching packs and playlists
- [ ] Category filters/tabs

### Pack Detail Page
- [ ] Pack name, description, creator name
- [ ] Image preview grid (all images in the pack)
- [ ] Tap image for full-screen preview
- [ ] Image count, download count
- [ ] "Download" button (free) or "Purchase" button (paid - future)
- [ ] Download progress indicator
- [ ] Download saves images to local storage

### Explore Playlist Detail Page
- [ ] Playlist name, description, creator name
- [ ] Preview images from the playlist
- [ ] Rules summary (list of triggers and actions in human-readable form)
- [ ] Subcollection list
- [ ] "Download" button (free) or "Purchase" button (paid - future)
- [ ] Download creates a local copy of the playlist with all images

---

## Phase 12: Profile/Settings Tab
> User account management and app settings.

### Unauthenticated State
- [ ] "Create Account" prompt explaining benefits (sync purchases, cross-device)
- [ ] "Sign In" button -> LoginPage
- [ ] "Continue without account" option (stays on settings-only view)
- [ ] App settings section still accessible
1
### Account Creation Flow
- [ ] Email input with validation
- [ ] Send verification code via email (Lightning Server PinHandler)
- [ ] Code entry screen
- [ ] Account created on successful verification
- [ ] Redirect to profile page

### Authenticated State
- [ ] Display user email and name
- [ ] Edit name
- [ ] "My Downloads" section (list of downloaded packs/playlists)
- [ ] "My Purchases" section (future - for paid content)
- [ ] "Sign Out" button

### App Settings (always visible)
- [ ] Active playlist selector (dropdown of user's playlists)
- [ ] Theme mode (Auto/Light/Dark)
- [ ] Dynamic theming toggle (extract colors from wallpaper)
- [ ] Notification preferences
- [ ] Wallpaper target (home screen, lock screen, both) - Android only
- [ ] About section (app version, credits)
- [ ] Clear cached data option
- [ ] Export/import playlists

---

## Phase 13: UI Improvements
> Polish and improve the UI beyond the original Android app.

### Improvements over Original
- [ ] Better onboarding: first-run tutorial or tips explaining the app concept
- [ ] Playlist preview: show a mini-timeline/schedule of when wallpapers will change
- [ ] Rule editor: inline editing instead of multi-screen wizard (optional improvement)
- [ ] Image grid: show which images are in which subcollections with color-coded borders
- [ ] Active playlist dashboard: show current wallpaper, next scheduled change, trigger status
- [ ] Animations: page transitions, list item animations, theme change transitions
- [ ] Error states: better error handling with retry buttons instead of toasts
- [ ] Loading states: skeleton loaders for image grids and lists
- [ ] Confirmation feedback: success animations after saving/deleting
- [ ] Responsive layout: adapt grid columns based on screen width
- [ ] Accessibility: proper content descriptions, keyboard navigation

---

## Phase 14: Testing & Polish
> Comprehensive testing and final polish before release.

- [ ] Unit tests for shared models (serialization/deserialization)
- [ ] Unit tests for playlist navigation logic (next, previous, random, wrap-around)
- [ ] Unit tests for weather condition matching
- [ ] Unit tests for trigger scheduling calculations
- [ ] Integration tests for server endpoints
- [ ] Integration tests for offline sync
- [ ] Manual testing on Android device/emulator
- [ ] Manual testing on iOS simulator
- [ ] Manual testing on web browser
- [ ] Manual testing on desktop
- [ ] Performance testing with large playlists (100+ images)
- [ ] Memory leak testing for image loading
- [ ] Battery usage testing for triggers/alarms
- [ ] Crash reporting setup (Firebase Crashlytics or equivalent)

---

## Phase 15: Purchase System (Future - Deferred)
> Payment integration for paid packs and playlists.

- [ ] Stripe integration for cross-platform payments
- [ ] Purchase model (id, userId, itemId, itemType, price, purchaseDate, platform)
- [ ] Server-side purchase validation
- [ ] Purchase restoration for account holders
- [ ] Creator payout system
- [ ] Price display in explore tab
- [ ] Purchase flow UI (payment sheet, confirmation, receipt)
- [ ] Platform IAP integration (Google Play, App Store) - optional alternative

---

## Notes

### Architecture Decisions
- **Offline-first**: All data persisted locally as JSON. Server sync is optional when user has account.
- **Platform-specific wallpaper**: Each platform implements wallpaper setting differently via expect/actual.
- **Shared business logic**: Rules, triggers, actions, and navigation logic all in commonMain.
- **Server is supplementary**: The app works fully offline. Server enables explore/marketplace, sync, and accounts.

### UI Improvements Identified
1. The original app uses a multi-screen wizard (6 screens) to create a single rule. Consider an inline or 2-step approach.
2. The original has no onboarding - new users may not understand the concept of triggers+actions.
3. The original shows no indication of when the next wallpaper change will happen.
4. The original has weak form validation across all trigger screens.
5. The original uses Toasts for feedback - KiteUI Actions provide better error handling with dialogs.
6. The original has no search or filtering for images.
7. The original has no way to see which images are used in which subcollections.
8. The original lacks loading indicators for async operations.
