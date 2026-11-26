# HyperZoom

Experimental milestone 0.1.0. **Not a native Samsung Camera zoom unlock yet.**

- Settings: custom maximum 1–1000×, saved locally.
- Dedicated CameraX photo test: uses the rear logical camera's supported zoom, followed by centered software magnification of live preview and saved photo. The saved image retains the remaining real pixels (no AI-generated detail).
- LSPosed diagnostic entry: scoped only to com.sec.android.app.camera; observes the confirmed ZoomView.setZoomValue(int) and Camera2 zoom/crop requests. Does not alter the stock camera or camera HAL.

The supplied APK contains ZoomView.setZoomValue(int), setZoomProperty(l9.b3) and obfuscated zoom manager interfaces. Their presence does not establish that changing UI limits changes photo output.

## Build
GitHub Actions produces a debug APK artifact. No Samsung APK is redistributed.
Locally: Gradle 8.11.1, JDK 17, Android SDK 35, gradle :app:assembleDebug.
## Test
Install the debug APK, set your maximum, open the test camera, grant camera permission and capture at several zoom levels. Inspect photos in Pictures/HyperZoom. For native diagnostics enable the module in LSPosed, select Samsung Camera only, force-stop and reopen Camera, change zoom, inspect LSPosed logs for HyperZoom. Disable the module to remove diagnostics. Camera integration remains a later milestone. Extremely high digital zoom loses detail; CameraX logical camera lens switching depends on firmware.
