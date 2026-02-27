# Dynamic Wallpaper - Feature List

## 1. Wallpaper Collection Management

### Creating Collections
Users can create named wallpaper collections that group multiple images together. When the user taps the floating action button (FAB) on the home screen, they are taken to the Add Collection screen where they name the collection, select images from the device gallery, and configure rules for automatic wallpaper changes. Each collection is stored as a Room database entity with a unique auto-generated ID. New collections are first saved to a temporary cache database during editing, and only committed to the main database when the user explicitly hits "Save." This prevents half-finished edits from corrupting saved data.

### Editing Collections
Existing collections can be reopened for editing. When an existing collection is loaded, a copy is placed into the cache database so all modifications happen on the staging copy. The user can rename the collection (live-updated via a TextWatcher), add or remove images, reorder photos, add or remove rules, and manage subcollections. Changes are only persisted to the main database upon saving.

### Deleting Collections
When editing an existing collection, a delete button appears in the toolbar. Tapping it shows a confirmation dialog. On confirmation, the collection is removed from the database and all associated image files are recursively deleted from internal storage.

### Home Screen Display
The home screen (HomeFragment) displays all saved collections in a RecyclerView list. If no collections exist, a placeholder message instructs the user to tap the plus button. The currently selected/active collection is displayed prominently at the top of the screen in a card showing its name.

---

## 2. Image Selection and Cropping

### Multi-Image Gallery Picker
Users can select multiple images at once from the device gallery using Android's `ACTION_GET_CONTENT` intent with `EXTRA_ALLOW_MULTIPLE` enabled. Both single and multi-selection are supported. Selected images are processed individually through the cropping workflow.

### Automatic Aspect-Ratio Cropping
Each selected image is sent to the UCrop library (via the `ucropnedit` fork) for cropping. The crop window is automatically configured to match the device's screen resolution and aspect ratio. The app calculates the screen dimensions, computes the greatest common denominator to derive the simplest aspect ratio, and sets UCrop's max resolution to the screen's pixel dimensions. This ensures every wallpaper image perfectly fits the device screen without stretching or black bars.

### Internal Storage Management
Cropped images are saved as JPEG files in the app's internal storage under a directory named after the collection's ID (`filesDir/<collectionId>/JPEG_<timestamp>.jpg`). Each image filename is tracked in the collection's `photoNames` list. This isolates each collection's images and allows clean deletion when a collection is removed.

---

## 3. Photo Order Management (Drag-and-Drop Reordering)

### Grid View of Collection Images
The ViewChangePhotoOrderFragment displays all images in a collection in a 3-column grid using a RecyclerView with GridLayoutManager. Each image is rendered using Glide for efficient loading.

### Drag-and-Drop Reordering
Users can long-press and drag images to reorder them. This is implemented via Android's `ItemTouchHelper` with a `SimpleCallback` supporting UP, DOWN, START, and END drag directions. When an image is moved, `Collections.swap()` reorders the `photoNames` list and the adapter is notified of the change. The last item in the list is protected from being dragged (likely an "add photo" placeholder).

### Individual Image Viewing and Deletion
Tapping an image in the grid navigates to ViewWallpaperFragment, which displays the image full-screen using Glide with `centerCrop()`. A delete button in the toolbar allows removing the image with a confirmation dialog. Deletion removes the filename from the collection's photo list, deletes the file from internal storage, and navigates back to the grid view.

---

## 4. Subcollections

### Creating Subcollections
Within a collection, users can create named subcollections -- subsets of the collection's images grouped under a label. The subcollection feature is toggled by a checkbox in the collection editor. When enabled, an "Add Subcollection" button appears. Tapping it navigates to SelectImagesForSubCollectionFragment, where the user names the subcollection and selects which images from the parent collection to include, displayed in a 3-column grid with selection indicators.

### Editing Subcollections
Existing subcollections can be reopened. The fragment detects whether it's creating new (subCollectionId == -1) or editing existing, and pre-fills the name and image selections accordingly.

### Subcollection Display
Subcollections are shown in a dedicated RecyclerView in the collection editor using a SubcollectionRecyclerViewAdapter, allowing users to see and manage all subcollections at a glance.

### Subcollection-Aware Wallpaper Navigation
When a subcollection is active (`selectedSubCollectionArrayIndex != -1`), wallpaper navigation (next, previous, random) operates within the subcollection's image list rather than the full collection. The collection tracks both the active subcollection index and the current image index within that subcollection separately.

---

## 5. Rules System (Trigger + Action)

### Rule Architecture
Each collection can have multiple rules. A rule is a pairing of a **Trigger** (when to change the wallpaper) and an **Action** (what change to make). Rules are displayed in a RecyclerView in the collection editor using RulesRecyclerAdapter, showing human-readable descriptions of both the trigger and action.

### Adding Rules
The rule creation flow is a multi-step wizard:
1. User taps "Add Rule" in the collection editor
2. SelectTriggersFragment presents 6 trigger type options as radio buttons
3. User configures the selected trigger in a dedicated fragment
4. SelectActionsFragment presents 4 action type options
5. The completed Rule (trigger + action pair) is saved to the collection

### Action Types
- **Next in Collection**: Advances to the next wallpaper in the current collection or subcollection sequentially, wrapping around to the beginning when the end is reached.
- **Switch to Subcollection**: Changes the active subcollection to a user-selected one (chosen via a bottom sheet dialog listing all subcollections) and displays the next image from it.
- **Random in Collection/Subcollection**: Picks a random wallpaper from the current image pool.
- **Specific Wallpaper**: Sets a particular image as the wallpaper (chosen via a bottom sheet dialog showing a 3-column grid of all images).

---

## 6. Trigger Types

### Time Interval Trigger
Configured in TriggerByTimeIntervalFragment. The user specifies:
- **Repeat interval amount**: A numeric value (e.g., 5)
- **Repeat interval type**: Minutes, Hour, Day, Week, or Month (selected via spinner)
- **Time to trigger**: A specific time of day selected via a TimePickerDialog (12-hour format)
- **Day of week** (weekly intervals only): Chip group allowing selection of specific days
- **Exact timing**: A checkbox to choose between exact alarms (`AlarmManager.setRepeating()`) or inexact alarms (`AlarmManager.setInexactRepeating()`), trading battery life for precision

The trigger sets up a repeating Android alarm. The interval is converted to milliseconds (Minutes=60000, Hour=3600000, Day=86400000, Week=604800000, Month≈2.628 billion ms). When the alarm fires, `AlarmActionReciever` processes it and executes the associated action.

### Date Trigger
Configured in TriggerByDateFragment. The user picks a specific calendar date (month and day) via a DatePickerDialog. The selected date is displayed as "MMM dd" format (e.g., "Jan 15"). This creates a repeating alarm that fires on that date annually. Supports both exact and inexact alarm modes.

### Season Trigger
Configured in BySeasonFragment. The user defines date ranges for up to four seasons (Winter, Spring, Summer, Fall). For each season:
- A checkbox enables/disables that season
- Calendar icon buttons open DatePickerDialogs for the start and end dates
- Dates are stored as month abbreviation + day (e.g., "Dec 21" to "Mar 20")

The trigger creates separate alarms for each enabled season. Each alarm's interval is calculated from the season's start date to end date, causing the wallpaper to change according to the associated action during that season's date range.

### Location Trigger (Geofencing)
Configured in TriggerLocationFragment. This trigger uses Google Play Services geofencing to change wallpaper when the user physically enters or exits a defined area. Features:
- **Interactive OpenStreetMap**: An osmdroid MapView displays an interactive map with MAPNIK tiles, multi-touch zoom, and built-in zoom controls
- **"Get My Location" button**: Uses GPS (`LocationManager`) to center the map on the user's current coordinates
- **Visual radius indicator**: A circle overlay sized to half the screen width shows the geofence area. The actual radius in meters is computed by measuring the geographic distance from the map center to the screen edge
- **Enter/Exit toggle**: Radio buttons let the user choose whether to trigger on entering or exiting the geofence area

The geofence is registered via `GeofencingClient` with a `PendingIntent` targeting `GeofenceBroadcastReceiver`. When the user crosses the geofence boundary, the receiver executes the associated action.

### Weather Trigger
Configured in TriggerByWeatherFragment. This is the most complex trigger type, using the National Weather Service (NWS) API for weather data. The user configures:

**Temperature conditions** (radio button selection):
- When temperature **is** a specific value
- When temperature is **less than** a value
- When temperature is **greater than** a value
- When temperature is **between** two values (low and high end)
- When a specific **weather condition** occurs (selected from a spinner: Clear, Mostly Clear, Clouds, Mostly Clouds, Rain, Light Rain, Snow, Light Snow, Mostly Sunny, Sunny, Rain And Snow, Thunderstorm, Fog, Windy)

**Location method** (radio button selection):
- **IP Address**: Resolves device location via the `ipapi.co` geolocation API
- **Current Location**: Uses the device's GPS via `LocationManager.getLastKnownLocation()`
- **Specific Set Location**: User enters latitude/longitude manually

**Forecast update interval**: How often to re-fetch the weather forecast and reschedule alarms (Hourly, 6 Hours, 12 Hours, 24 Hours, or 2 Days)

**How it works internally:**
1. The trigger determines location (via IP, GPS, or manual entry)
2. It calls `api.weather.gov/points/{lat},{lon}` to get the forecast endpoint URL
3. It fetches the hourly forecast JSON from the NWS API
4. It iterates through forecast periods, checking each against the configured condition
5. For every period that matches, it sets an individual `AlarmManager` alarm at that period's start time
6. When a forecast update alarm fires, `AlarmActionReciever` removes old weather alarms and re-fetches the forecast to schedule new ones

### Calendar Event Trigger (Placeholder)
The TriggerCalendarEventFragment exists in the codebase as a stub. The UI layout is inflated but contains no functional logic, no data model, and no navigation to the actions screen. This feature is planned but not yet implemented.

---

## 7. Quick Settings Tiles

### Next Wallpaper Tile
`GoToNextWallpaperTileService` registers an Android Quick Settings tile in the notification shade. When tapped, it reads the currently selected collection from SharedPreferences, fetches it from the database, calls `goToNextWallpaper()` to advance to the next image in sequence, saves the updated index back to the database, and shows a "Wallpaper Changed" toast.

### Previous Wallpaper Tile
`GoToPreviousWallpaperTileService` works identically to the Next tile but calls `goToPreviousWallpaper()` to go back one image in the sequence.

### Shuffle/Random Wallpaper Tile
`ShuffleWallpaperTileService` works identically but calls `goToRandWallpaper()` to select a random image from the current collection or subcollection.

All three tiles read the active collection from SharedPreferences (key: `"selectedCollection"` in `"sharedPrefrences"`). If no collection is selected, they show a "No Selected Collection" toast and do nothing.

---

## 8. Boot Persistence

### Trigger Restoration After Reboot
Android alarms and geofences do not survive device reboots. The `BootCompleteIntentReciever` BroadcastReceiver listens for `BOOT_COMPLETED` and `QUICKBOOT_POWERON` intents. When the device starts up:
1. It reads the selected collection ID from SharedPreferences
2. If no collection is selected, it shows a toast and exits
3. Otherwise, it fetches the collection from the database and calls `startTriggers()` to re-register all alarms and geofences

This ensures wallpaper automation resumes seamlessly after a reboot without user intervention.

---

## 9. Wallpaper Setting Engine

### Direct Wallpaper Application
The `Collection.setWallpaper(Context, File)` method handles the actual wallpaper change. It reads the image file as a `Bitmap` via `FileInputStream` and calls `WallpaperManager.getInstance(context).setBitmap(bitmap)` to set it as the device's home screen wallpaper.

### Navigation Methods
- **`goToNextWallpaper()`**: Increments the image index (within the active subcollection or top-level photos). Wraps to index 0 when past the end.
- **`goToPreviousWallpaper()`**: Decrements the image index. Wraps to 0 if the index goes below 0.
- **`goToRandWallpaper()`**: Uses `Random` to pick a random index from the available images.
- **`goToSpecificWallpaper()`**: Sets a wallpaper by exact filename.
- **`goToSpecificSubCollection()`**: Switches the active subcollection by name and advances to the next image within it.

### Action Dispatching
`Collection.runAction(int actionIndex, Context)` looks up the rule at the given index, reads the action type, and dispatches to the appropriate navigation method. This is called by `AlarmActionReciever` and `GeofenceBroadcastReceiver` when triggers fire.

---

## 10. Material You / Dynamic Colors

### System-Wide Dynamic Theming
The app supports Android 12+ Material You dynamic colors. `DynamicWallpaperApplication.onCreate()` calls `DynamicColors.applyToActivitiesIfAvailable()` to globally opt all activities into dynamic theming. On Android 12+, `MainActivity` also applies colors via a custom `DynamicColorUtils` utility. This means the app's color scheme automatically adapts to match the user's current wallpaper colors, creating a cohesive look.

### Dark Mode Support
The app includes separate resource directories for dark mode (`values-night/`) with dedicated dimensions and theme definitions, allowing the UI to adapt between light and dark system themes.

---

## 11. Responsive Layout Support

The app includes resource qualifiers for multiple screen configurations:
- **Default**: Standard phone layout
- **Landscape** (`values-land/`): Adjusted dimensions for landscape orientation
- **Tablet 600dp** (`values-w600dp/`): Optimized spacing for medium tablets
- **Large Screen 1240dp** (`values-w1240dp/`): Optimized for large tablets and foldables

---

## 12. Data Persistence

### Dual Database Architecture
The app uses two separate Room databases:
- **Main Database** (`RoomDB`, named `"collections"`): Stores finalized, saved collections
- **Cache Database** (`CacheRoomDB`, named `"collections-cache"`): Stores in-progress edits as staging copies

Both databases share the same schema (`Collection` entity) and DAO (`MainDao`). The cache pattern enables non-destructive editing -- users can make changes and cancel without affecting saved data.

### Cache Cleanup
Cache cleanup occurs in multiple places:
- **`MainActivity.onDestroy()`**: Calls `deleteAllItemsInCache()` to wipe all cache entries when the app closes. If a cached collection doesn't exist in the main database (was never saved), its associated image files are also deleted from storage.
- **`HomeFragment.onCreate()`**: Deletes any lingering cache entries from previous sessions on startup.

### Custom Serialization
Complex model objects (Rules, SubCollections, photo name lists) are serialized to strings using custom delimiter-based encoding (e.g., `~triggerTypeDeliminator~`, `~triggersAndActions~`, `~subCollectionFileNames~`) via Room TypeConverters. This allows storing rich object graphs in SQLite text columns.

---

## 13. Firebase Integration

### Crashlytics
The app integrates Firebase Crashlytics for automatic crash reporting. This captures unhandled exceptions and sends crash reports to the Firebase console for debugging.

### Analytics
Firebase Analytics is included for tracking app usage patterns and events.

---

## 14. Debug/Testing Tools

### Debug Fragment
A DebugFragment (written in Kotlin) provides a developer testing interface accessible from the home screen's selected collection card. It contains a button that exercises the weather trigger pipeline end-to-end:
1. Creates a `TriggerByWeather` configured for temperature > 80 degrees using GPS location
2. Fetches the device's current coordinates
3. Queries the NWS weather API
4. Sets up prediction-based alarms

This runs on a background thread to avoid blocking the UI during network calls, and allows developers to test the weather trigger system without waiting for real weather conditions.

---

## 15. Notification Channel

### Android O+ Notification Support
`MainActivity` creates a notification channel (`"channel_id"`, named "My Channel") on Android 8.0+ (API 26+). This is required for posting notifications on newer Android versions and supports the app's trigger-related notifications.
