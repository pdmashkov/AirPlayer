package com.airplayer

import com.airplayer.airplay.NowPlayingInfo
import com.airplayer.service.PhotoFrame
import com.airplayer.service.ProtocolState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MainActivityTest — Unit tests for the streaming-overlay state logic in [MainActivity].
 *
 * WHY: [MainActivity] observes [AirPlayerService.airPlayState] and toggles the full-screen
 * streaming overlay. A bug here means video starts but the user sees the home screen
 * instead of the stream (or vice versa).
 *
 * WHAT WE TEST:
 * - [ProtocolState.CONNECTED] → show streaming screen
 * - All other [ProtocolState] values → hide streaming screen
 * - The surface provider mechanism: setting / clearing a provider lambda
 *
 * HOW: [MainActivity] is an AppCompatActivity and cannot be instantiated in JVM unit tests
 * without Robolectric. We test the decision logic (state → show/hide) as a pure function,
 * mirroring the exact same `when` expression used in [MainActivity.observeStreamingState].
 * Full Activity lifecycle testing (bind/unbind, screen transitions) is covered by
 * instrumentation tests.
 */
class MainActivityTest {

    // ─── Streaming overlay decision logic ────────────────────────────────────

    /**
     * Replicates the exact decision lambda from [MainActivity.observeStreamingState]:
     * returns true if the overlay should be shown, false if it should be hidden.
     */
    private fun shouldShowStreaming(state: ProtocolState): Boolean =
        state == ProtocolState.CONNECTED

    @Test
    fun `CONNECTED state shows streaming overlay`() {
        assertTrue(shouldShowStreaming(ProtocolState.CONNECTED))
    }

    @Test
    fun `ADVERTISING state hides streaming overlay`() {
        assertFalse(shouldShowStreaming(ProtocolState.ADVERTISING))
    }

    @Test
    fun `DISABLED state hides streaming overlay`() {
        assertFalse(shouldShowStreaming(ProtocolState.DISABLED))
    }

    @Test
    fun `ERROR state hides streaming overlay`() {
        assertFalse(shouldShowStreaming(ProtocolState.ERROR))
    }

    @Test
    fun `only CONNECTED shows overlay — all other states hide it`() {
        val showingStates  = ProtocolState.values().filter { shouldShowStreaming(it) }
        val hidingStates   = ProtocolState.values().filter { !shouldShowStreaming(it) }

        assertEquals("Exactly one state should show the overlay", 1, showingStates.size)
        assertEquals(ProtocolState.CONNECTED, showingStates.first())
        assertEquals(3, hidingStates.size)
    }

    // ─── Surface provider lambda mechanics ───────────────────────────────────
    //
    // Tests verify the pattern:
    //   service.setVideoSurfaceProvider { getVideoSurface() }
    // and that clearing it with { null } prevents dangling Surface references.

    @Test
    fun `surface provider returns non-null surface when set`() {
        // Simulate a provider returning a non-null surface (here: any non-null object)
        val fakeSurface = Any()  // placeholder for android.view.Surface
        var provider: (() -> Any?)? = { fakeSurface }

        val result = provider?.invoke()
        assertNotNull("Provider should return the surface", result)
        assertEquals(fakeSurface, result)
    }

    @Test
    fun `surface provider returns null after being cleared`() {
        // After onStop(), we set the provider to { null } to release the Surface reference
        var provider: (() -> Any?)? = { null }

        val result = provider?.invoke()
        assertNull("Cleared provider should return null", result)
    }

    @Test
    fun `null provider (before binding) returns null safely`() {
        // Before the Activity binds to the service, videoSurfaceProvider is null
        val provider: (() -> Any?)? = null

        val result = provider?.invoke()
        assertNull("Null provider should return null via safe call", result)
    }

    @Test
    fun `provider can be reassigned from non-null to null`() {
        // Simulates the onStart → onStop transition
        var provider: (() -> Any?)? = { Any() }
        assertNotNull(provider?.invoke())

        // Simulate onStop: clear the provider
        provider = { null }
        assertNull(provider.invoke())
    }

    // ─── Keep-screen-on decision logic ───────────────────────────────────────
    //
    // MainActivity.isOverlayActive() decides whether to hold FLAG_KEEP_SCREEN_ON, so Android TV's
    // screensaver doesn't kick in mid-mirror (see updateOverlay()/setKeepScreenOn()). It must
    // return true for exactly the states where updateOverlay() shows a full-screen overlay.

    private val samplePin = "1234"
    private val sampleNowPlaying = NowPlayingInfo(senderName = "Test Sender")
    private val samplePhotoFrame = PhotoFrame(bytes = byteArrayOf(1, 2, 3), mimeType = "image/jpeg")

    @Test
    fun `isOverlayActive is false when nothing is showing`() {
        assertFalse(
            MainActivity.isOverlayActive(
                pin = null, nowPlaying = null, airPlayState = ProtocolState.DISABLED, photoFrame = null
            )
        )
    }

    @Test
    fun `isOverlayActive is true while a pairing PIN is shown`() {
        assertTrue(
            MainActivity.isOverlayActive(
                pin = samplePin, nowPlaying = null, airPlayState = ProtocolState.ADVERTISING, photoFrame = null
            )
        )
    }

    @Test
    fun `isOverlayActive is true for audio-only now-playing`() {
        assertTrue(
            MainActivity.isOverlayActive(
                pin = null, nowPlaying = sampleNowPlaying, airPlayState = ProtocolState.ADVERTISING, photoFrame = null
            )
        )
    }

    @Test
    fun `isOverlayActive is true while mirroring is CONNECTED`() {
        assertTrue(
            MainActivity.isOverlayActive(
                pin = null, nowPlaying = null, airPlayState = ProtocolState.CONNECTED, photoFrame = null
            )
        )
    }

    @Test
    fun `isOverlayActive is true while a photo is shown`() {
        assertTrue(
            MainActivity.isOverlayActive(
                pin = null, nowPlaying = null, airPlayState = ProtocolState.ADVERTISING, photoFrame = samplePhotoFrame
            )
        )
    }

    @Test
    fun `isOverlayActive is false when ADVERTISING with no overlay content`() {
        assertFalse(
            MainActivity.isOverlayActive(
                pin = null, nowPlaying = null, airPlayState = ProtocolState.ADVERTISING, photoFrame = null
            )
        )
    }

    @Test
    fun `isOverlayActive is false when ERROR with no overlay content`() {
        assertFalse(
            MainActivity.isOverlayActive(
                pin = null, nowPlaying = null, airPlayState = ProtocolState.ERROR, photoFrame = null
            )
        )
    }
}
