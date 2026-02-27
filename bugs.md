# Bugs Found in Current Android Codebase

These bugs exist in the current Java/Android implementation. They do NOT need to be fixed in the old codebase since we are rewriting, but they should NOT be carried over to the new implementation.

## Critical

- [ ] **AlarmActionReciever.java**: Uses `==` for string comparison instead of `.equals()` when checking `"triggerByWeather"` trigger type. This means the weather trigger branch is never reached; it always falls through to the else branch.
- [ ] **TriggerByWeatherFragment.java (line ~173/197)**: `weatherTypes.equals("specificSetLocation")` compares an `ArrayList<String>` object to a `String`, which always returns `false`. This means the specific-location validation is never enforced, allowing users to proceed without entering valid coordinates.
- [ ] **BySeasonFragment.java - getDates()**: Both `winterStartTemp` and `winterEndTemp` read from `winterStart1textView` (the end date text view). The winter start date is lost, and the end date is used for both start and end.

## Moderate

- [ ] **TriggerByDateFragment.java**: Snackbar is created on validation failure but `.show()` is never called, so the user sees no error message.
- [ ] **TriggerByDateFragment.java - requiredFieldsFilled()**: `String.split(" ")` on an empty string returns an array of length 1, so this validation always returns `true` and never catches empty input.
- [ ] **TriggerBySeason.java - Season.setCalMonth()**: The month "Jun" (June) does not handle the `isEndMonth` case, so June as an end month gets the wrong calendar integer.
- [ ] **Collection.java - goToPreviousWallpaper()**: When the index goes below 0, it wraps to index 0 instead of wrapping to the last image in the list. Should wrap to `size - 1`.
- [ ] **Converters.java**: The Map-to-String serialization uses commas and `<divider>` as delimiters. Any key or value containing a comma or the literal string `<divider>` will corrupt the data.

## Minor

- [ ] **ShuffleWallpaperTileService.java**: `onStartListening()` does not set the tile state to `Tile.STATE_ACTIVE`, unlike the other two tile services (`GoToNextWallpaperTileService` and `GoToPreviousWallpaperTileService`). This may cause the tile to appear inactive/greyed out.
- [ ] **AddCollectionFragment.java - setRandomImagesForPreview()**: When there are fewer than 5 photos, `photoCount` is clamped to 5 which could cause index issues, though this is partially guarded by size checks.
