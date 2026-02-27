# KiteUI Skill Improvements - Image & File Picker APIs

This document captures KiteUI APIs for image handling, file picking, and related patterns
that are not well-documented in the knowledge base. Use this as a reference when working
with images and files in KiteUI apps.

## File Picking

KiteUI provides cross-platform file picking via `RContext` extension functions.

### API Signatures (expect/actual - platform-implemented)

```kotlin
// Pick a single file
expect suspend fun RContext.requestFile(mimeTypes: List<String> = listOf("*/*")): FileReference?

// Pick multiple files
expect suspend fun RContext.requestFiles(mimeTypes: List<String> = listOf("*/*")): List<FileReference>

// Capture from front camera (selfie)
expect suspend fun RContext.requestCaptureSelf(mimeTypes: List<String> = listOf("image/*")): FileReference?

// Capture from rear camera (environment)
expect suspend fun RContext.requestCaptureEnvironment(mimeTypes: List<String> = listOf("image/*")): FileReference?
```

### Usage Pattern

```kotlin
// Single image pick
val image = Signal<ImageSource?>(null)

button {
    text { content = "Pick Image" }
    onClick {
        image.value = context.requestFile(listOf("image/*"))?.let { ImageLocal(it) }
    }
}

// Multiple image pick
button {
    text { content = "Pick Images" }
    onClick {
        val files = context.requestFiles(listOf("image/*"))
        // files is List<FileReference>
        // Wrap each as ImageLocal(file) for display
    }
}
```

### Key Points
- `context` is the `RContext` available in `onClick` blocks
- Returns `null` if user cancels
- `requestFiles` returns empty list if cancelled
- MIME type `"image/*"` filters to images only
- `"*/*"` allows any file type

## Image Display

### ImageSource Hierarchy

```kotlin
expect sealed class ImageSource(): VisualMediaSource

// From a picked FileReference
data class ImageLocal(val file: FileReference) : ImageSource()

// From a URL
data class ImageRemote(val url: String) : ImageSource()

// From raw bytes (Blob)
data class ImageRaw(val data: Blob) : ImageSource()

// From bundled app resources
expect class ImageResource : ImageSource

// SVG vector (used for icons)
data class ImageVector(...) : ImageSource()
```

### Displaying Images

```kotlin
// Static image
image {
    source = ImageLocal(fileRef)
    scaleType = ImageScaleType.Crop  // or .Stretch, .Fit, etc.
}

// Reactive image (from Signal)
image {
    ::source { myImageSignal() }
    scaleType = ImageScaleType.Crop
}

// With size constraints
sizeConstraints(width = 6.rem, height = 6.rem).image {
    source = ImageLocal(fileRef)
    scaleType = ImageScaleType.Crop
}

// From resource
image {
    source = Resources.imagesSnowyBackground
    scaleType = ImageScaleType.Crop
}

// From raw bytes (e.g., loaded from storage)
val gif = rememberSuspending {
    ImageRaw(Resources.imagesGifTest())
}
image {
    ::source { gif() }
}
```

### ImageScaleType Options
- `ImageScaleType.Crop` - Fill the container, crop overflow
- `ImageScaleType.Stretch` - Stretch to fill (may distort)
- `ImageScaleType.Fit` - Fit inside container, may letterbox
- `ImageScaleType.NoScale` - Original size

## FileReference

`FileReference` is a platform-specific handle to a picked file.

```kotlin
// Get MIME type
val mimeType: String = fileReference.mimeType()

// Get file name
val name: String = fileReference.fileName()

// Get file size in bytes
val size: Long = fileReference.bytes()

// Wrap for display
val imageSource = ImageLocal(fileReference)
```

### Platform-Specific FileReference Implementations

**Android**: `actual class FileReference(val uri: Uri)` - wraps an Android `Uri`.
- To read bytes: `context.contentResolver.openInputStream(fileReference.uri)!!.readBytes()`
- To create from a saved file: `FileReference(Uri.fromFile(javaFile))` (pass `Uri`, NOT a `String`)
- `Uri.parse(string)` returns a `Uri`, use it only when you have a URI string

**JS**: `FileReference` wraps a browser `Blob`/`File` object.
- Cast via `file.asDynamic() as org.w3c.files.Blob` to access raw blob
- Use `FileReader` to read as data URL or array buffer

**iOS**: `FileReference` wraps a file path string.
- Access via `file.filePath` property
- Use `NSFileManager` for file operations

### Persisting Images

`FileReference` is transient - it doesn't survive app restarts. To persist images:

1. Read the file bytes from the `FileReference`
2. Save bytes to local storage (platform-specific)
3. Store a reference ID in your data model
4. On load, read bytes back and create `ImageRaw(blob)` or `ImageLocal(savedFileRef)`

Example pattern from another app:
```kotlin
data class EditCreateImageWrapper(
    val reference: FileReference? = null,     // Transient picked file
    val localPath: String? = null,            // Persisted local path
    val uploadedImage: ImageSource? = null,   // Server-uploaded image
    val serverFileLocation: String? = null,   // Server URL
)
```

## Other RContext Utilities

```kotlin
// Clipboard
fun RContext.setClipboardText(value: String)

// Download files
suspend fun RContext.download(name: String, blob: Blob, preferredDestination: DownloadLocation = DownloadLocation.Downloads)
suspend fun RContext.download(name: String, url: String, preferredDestination: DownloadLocation = DownloadLocation.Downloads, onDownloadProgress: ((progress: Float) -> Unit)? = null)

// Share
fun RContext.share(namesToBlobs: List<Pair<String, Blob>>)
fun RContext.share(title: String, message: String? = null, url: String? = null)

// Open native apps
fun RContext.openEvent(title: String, description: String, location: String, start: LocalDateTime, end: LocalDateTime, zone: TimeZone)
fun RContext.openMap(latitude: Double, longitude: Double, label: String? = null, zoom: Float? = null)
```

## Dialogs & Confirmations

```kotlin
// Simple confirmation (returns Boolean)
val confirmed = confirm("Delete this item?")

// Danger confirmation with action
confirmDanger("Delete Playlist", "This will permanently delete the playlist and all its images.") {
    // This block runs if user confirms
    deletePlaylist()
}

// Custom dialog
dialog { close ->
    card.col {
        h2 { content = "Custom Dialog" }
        text { content = "Dialog content here" }
        row {
            button { text { content = "Cancel" }; onClick { close() } }
            important.button { text { content = "OK" }; onClick { doThing(); close() } }
        }
    }
}

// Bottom sheet
onClick {
    openBottomSheet {
        col {
            h3 { content = "Options" }
            button { text { content = "Option 1" }; onClick { dismissBackground() } }
        }
    }
}

// Toast
toast("Operation completed")

// Alert
alert("Something happened")
```

**Important**: Dialog lambda is NOT a suspend context. Capture async values BEFORE the dialog:
```kotlin
onClick {
    val price = request().price  // suspend OK here
    dialog { close ->
        text { content = "Price: $price" }  // NOT suspend here
    }
}
```

## HTTP Networking - KiteUI `fetch()`

KiteUI provides a multiplatform `fetch()` function for making HTTP requests across all targets (Android, iOS, JS, JVM).

### Import & Basic Usage

```kotlin
import com.lightningkite.kiteui.fetch
import com.lightningkite.kiteui.HttpMethod
import com.lightningkite.kiteui.httpHeaders

// Simple GET request
val response = fetch("https://api.example.com/data")
val body = response.text()

// GET with headers
val response = fetch(
    url = "https://api.example.com/data",
    headers = httpHeaders("Accept" to "application/json")
)

// POST with JSON body
val response = fetch(
    url = "https://api.example.com/data",
    method = HttpMethod.POST,
    body = jsonString,
    type = "application/json"
)
```

### Core API Signatures

```kotlin
// Main expect function (all platforms implement this)
expect suspend fun fetch(
    url: String,
    method: HttpMethod = HttpMethod.GET,
    headers: HttpHeaders = httpHeaders(),
    body: RequestBody? = null,
    onUploadProgress: ((bytesComplete: Long, bytesExpectedOrNegativeOne: Long) -> Unit)? = null,
    onDownloadProgress: ((bytesComplete: Long, bytesExpectedOrNegativeOne: Long) -> Unit)? = null,
): RequestResponse

// Convenience overloads for text body
suspend fun fetch(url, method, headers, type: String = "text/plain", body: String): RequestResponse
// Convenience overloads for Blob and FileReference bodies also exist
```

### RequestResponse

```kotlin
class RequestResponse {
    val status: Short        // HTTP status code
    val ok: Boolean          // true if 2xx
    val headers: HttpHeaders
    suspend fun text(): String   // Read body as text
    suspend fun blob(): Blob     // Read body as binary blob
}
```

### HttpHeaders

```kotlin
// Create headers from pairs
val headers = httpHeaders("Content-Type" to "application/json", "Accept" to "application/json")

// Create from map
val headers = httpHeaders(mapOf("Authorization" to "Bearer token"))

// Mutate
headers.set("X-Custom", "value")
headers.append("X-Custom", "another")
headers.delete("X-Custom")
headers.get("X-Custom") // returns String? (comma-joined if multiple)
headers.has("X-Custom") // returns Boolean
```

### HttpMethod Enum
`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `HEAD`

### Error Handling
- Throws `ConnectionException(message, cause)` on network failures
- On Android: has built-in retry logic (5 attempts) for `UnknownHostException` after sleep/lock
- Check `response.ok` or `response.status` for HTTP-level errors

### Platform Implementations
- **Android**: Uses ktor `HttpClient` with CIO engine via `AndroidAppContext.ktorClient`
- **iOS**: Uses ktor `HttpClient` with Darwin engine
- **JS**: Uses `XMLHttpRequest` directly (not ktor)
- **JVM**: Uses ktor `HttpClient` with OkHttp engine

### Key Points
- Fully multiplatform - same `fetch()` call works on all targets
- No need to add ktor as a direct dependency - KiteUI includes it
- `body` parameter accepts `RequestBodyText`, `RequestBodyBlob`, or `RequestBodyFile`
- Progress callbacks available for upload/download monitoring
- WebSocket support via `websocket(url: String): WebSocket`

## Theme API - Customization & Dynamic Theming

### Theme Class Structure

`Theme` is an immutable class (equality based on `id` string). Key properties:
- `foreground: Paint`, `background: Paint`, `outline: Paint` - all accept `Color` (which implements `Paint`)
- `font: FontAndStyle`, `elevation: Dimension`, `cornerRadii: CornerRadii`
- `gap: Dimension`, `padding: Edges`
- `semanticOverrides: SemanticOverrides`

### Color Class

```kotlin
// Color(alpha, red, green, blue) - all 0-1 floats
val c = Color(1f, 0.5f, 0.2f, 0.8f)

// Utilities
c.perceivedBrightness  // Float - perceptual brightness
c.toHSV()              // -> HSVColor
c.highlight(0.2f)      // Darkens if bright, lightens if dark
c.darken(0.2f)         // Multiply RGB by (1 - ratio)
c.lighten(0.2f)        // Move RGB toward 1.0
c.applyAlpha(0.5f)     // Multiply alpha
c.invert()             // 1 - each channel

// Factory methods
Color.fromHex(0xFFB00020.toInt())  // From hex int (no alpha prefix needed)
Color.fromHexString("#B00020")     // From hex string
Color.interpolate(a, b, 0.5f)     // Linear interpolation
Color.gray(0.5f)                   // Grayscale
```

### HSVColor

```kotlin
val hsv = HSVColor(alpha = 1f, hue = Angle(0.5f), saturation = 0.8f, value = 0.9f)
val rgb = hsv.toRGB()  // -> Color
val hsv2 = Color.red.toHSV()  // Color -> HSVColor
```

### Theme Builders

Available factory methods on `Theme.Companion`:
- `Theme.flat(id, hue, saturation, baseBrightness)` - Flat style with single hue
- `Theme.flat2(id, hue)` - Flat style variant (takes `Angle` for hue)
- `Theme.material(id, primary, secondary, ...)` - Material Design style
- `Theme.material3(id, primary, secondary, backgroundAdjust, ...)` - Material 3 style
- `Theme.clean()` - Clean minimal style
- `Theme.shadCnLike(name)` - ShadCN-inspired style
- `Theme.random()` - Random theme for testing

### Creating Derived Themes

```kotlin
// customize() - creates new theme from base with specified overrides
val custom = baseTheme.customize(
    newId = "my-theme",
    background = Color(1f, 0.95f, 0.95f, 0.95f),
    foreground = Color(1f, 0.1f, 0.1f, 0.1f),
    outline = Color(1f, 0.5f, 0.5f, 0.5f),
    semanticOverrides = SemanticOverrides(...)
)

// copy() - derives theme, chaining ID: "${parent.id}-${newId}"
val derived = baseTheme.copy(
    id = "variant",
    background = Color.white,
    foreground = Color.black
)
```

### SemanticOverrides

Override how built-in semantics (ImportantSemantic, DangerSemantic, etc.) style elements:

```kotlin
val overrides = SemanticOverrides(
    ImportantSemantic.override { theme ->
        // 'this' is ImportantSemantic, 'theme' is the Theme being styled
        // withBack() is Semantic.Theme.withBack() - extension on Theme inside Semantic
        theme.withBack(
            background = accentColor,
            foreground = contrastColor,
            outline = accentColor.highlight(0.1f)
        )
    }
)

val themed = baseTheme.customize(
    newId = "branded",
    semanticOverrides = overrides
)
```

**Important**: `withBack()` and `withoutBack()` are extension functions on `Theme` defined inside `Semantic`. They're only accessible inside a `Semantic` context (e.g., inside `override { }` lambdas or `Semantic.default()` implementations).

### Key Semantics Available

- `ImportantSemantic` - Primary actions (inverts fg/bg by default)
- `CardSemantic` - Card containers
- `BarSemantic` / `NavSemantic` - Toolbars/navigation
- `DangerSemantic` - Destructive actions (red)
- `WarningSemantic` - Warnings (orange)
- `AffirmativeSemantic` - Positive actions (green)
- `DisabledSemantic` - Disabled state
- `HoverSemantic` / `DownSemantic` - Interaction states
- `FieldSemantic` - Input fields
- `SubtextSemantic` - Secondary text

### Reactive Theme Application

```kotlin
// Global theme signal
val appTheme = Signal<Theme>(defaultTheme)

// Set reactively - entire UI updates
appTheme.value = newTheme
```

### Pitfalls

- `reactiveScope { }` is NOT a suspend context - cannot call suspend functions inside
- `::checked { signal() }` reactive binding may not work with `Signal` from `com.lightningkite.reactive.core` the same way as KiteUI's internal reactives
- Prefer `checked bind signal` for two-way switch binding (established pattern)
- `kotlin.math.pow` is `Double.pow(Double)` - for Float, convert: `floatVal.toDouble().pow(2.4).toFloat()`

## Conditional Visibility

### `shownWhen` (preferred) vs `onlyWhen` (deprecated)

```kotlin
// Correct pattern - use shownWhen with dot syntax
shownWhen { condition() }.col {
    // content shown/hidden reactively
}

// Can chain with other modifiers
shownWhen { isVisible() }.sizeConstraints(height = 10.rem).image {
    source = ImageRemote(url)
}

// DEPRECATED - onlyWhen still compiles but should not be used
// onlyWhen { condition() } - view { }  // minus operator syntax - may cause issues
```

**Key points:**
- `shownWhen` returns a `ViewWriter` - chain with `.viewType { }` (dot syntax)
- Do NOT use `onlyWhen { } - viewType { }` (minus operator syntax) - it's deprecated and can cause "None of the following candidates is applicable" errors
- `shownWhen(default = false) { condition }` - optional `default` param controls initial visibility before first reactive evaluation

## Stack Layout → Frame

`stack` is deprecated in KiteUI 7.x. Use `frame` instead:

```kotlin
// DEPRECATED
stack {
    image { ... }
    atTopStart.button { ... }
}

// CORRECT
frame {
    image { ... }
    atTopStart.button { ... }
}
```

### Stack/Frame Alignment Modifiers
- `atTopStart`, `atTopCenter`, `atTopEnd`
- `atCenterStart`, `centered`, `atCenterEnd`
- `atBottomStart`, `atBottomCenter`, `atBottomEnd`

## ImageVector.placeholder - Does NOT Exist

There is no `ImageVector.placeholder` in KiteUI. For fallback images:
- Use `ImageRemote("")` as a no-op fallback (when behind `shownWhen`)
- Or use an `Icon` constant: `Icon.dot`

## Blob.toByteArray()

`Blob.toByteArray()` is a suspend extension function from KiteUI:
```kotlin
import com.lightningkite.kiteui.toByteArray

val bytes: ByteArray = blob.toByteArray()
```
Available on all platforms. Used for converting downloaded blobs to byte arrays for local storage.
