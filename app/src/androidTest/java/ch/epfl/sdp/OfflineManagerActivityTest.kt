package ch.epfl.sdp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MapActivity.Companion.MAP_READY_DESCRIPTION
import ch.epfl.sdp.MapActivityTest.Companion.MAP_LOADING_TIMEOUT
import ch.epfl.sdp.ui.maps.MapUtils.getCameraWithParameters
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineManagerActivity
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineRegionUtils.getRegionName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OfflineManagerActivityTest {
    private lateinit var mUiDevice: UiDevice

    companion object {
        private const val RANDOM_NAME = "RandomName"
        private const val SEA_NAME = "SEA"
        private val SEA: LatLng = LatLng(39.317261, 6.485201)
        private const val EPSILON = 1e-3
        private const val POSITIVE_BUTTON_ID: Int = android.R.id.button1
        private const val NEGATIVE_BUTTON_ID: Int = android.R.id.button2
        private const val NEUTRAL_BUTTON_ID: Int = android.R.id.button3
        val context = MainApplication.applicationContext()
        val offlineManager: OfflineManager? = OfflineManager.getInstance(context)
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(OfflineManagerActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        moveCameraToPosition(LatLng(0.0, 0.0))
        UiThreadStatement.runOnUiThread {
            mUiDevice.wait(Until.hasObject(By.desc(MAP_READY_DESCRIPTION)), MAP_LOADING_TIMEOUT)
        }
    }

    private fun moveCameraToPosition(pos: LatLng) {
        UiThreadStatement.runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = getCameraWithParameters(pos, 15.0)
                mapboxMap.cameraPosition = getCameraWithParameters(pos, 15.0)
            }
        }
    }

    @Test
    fun checktToastWhenNoMapsHaveBeenDownloaded() {
        onView(withId(R.id.list_button)).perform(click())
        onView(withText(context.getString(R.string.toast_no_regions_yet)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    /**
     * this also tests that Toast are shown
     */
    @Test
    fun canDownloadAndThenDeleteMap() {
        //check that the downloaded list map is empty
        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }
            override fun onError(error: String) {} //left intentionally empty
        })

        //DOWNLOAD part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(RANDOM_NAME))
        mUiDevice.pressBack()
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        onView(withText(context.getString(R.string.end_progress_success)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                //check that the region has been downloaded
                assertThat(RANDOM_NAME, equalTo(getRegionName(offlineRegions[0])))
            }
            override fun onError(error: String) {} //left intentionally empty
        })

        onView(withId(R.id.list_button)).perform(click())
        onView(withId(NEGATIVE_BUTTON_ID)).perform(click())

        //DELETE PART
        onView(withId(R.id.list_button)).perform(click())
        onView(withId(NEUTRAL_BUTTON_ID)).perform(click())
        onView(withText(context.getString(R.string.toast_region_deleted)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        //check that the downloaded list map is empty
        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }
            override fun onError(error: String) {} //left intentionally empty
        })
    }

    /**
     * We move the camera over CMA
     * Download CMA map
     * Then we move the camera somewhere random on the globe
     * And finally we try to navigate back to CMA
     *
     * this also tests that Toast are shown
     */
    @Test
    fun canNavigateToDownloadedMap() {
        val rdmLatLng = LatLng((-90..90).random().toDouble(), (-180..180).random().toDouble())

        moveCameraToPosition(SEA)

        //check that the downloaded list map is empty
        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }
            override fun onError(error: String) {} //left intentionally empty
        })

        //DOWNLOAD Part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(SEA_NAME))
        mUiDevice.pressBack()
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        onView(withText(context.getString(R.string.end_progress_success)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        moveCameraToPosition(rdmLatLng)

        //NAVIGATE Part
        onView(withId(R.id.list_button)).perform(click())
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        onView(withText(SEA_NAME))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
            assertThat(mapboxMap.cameraPosition.target.latitude, Matchers.closeTo(SEA.latitude, EPSILON))
            assertThat(mapboxMap.cameraPosition.target.longitude, Matchers.closeTo(SEA.longitude, EPSILON))
        }

        //DELETE PART
        onView(withId(R.id.list_button)).perform(click())
        onView(withId(NEUTRAL_BUTTON_ID)).perform(click())
        onView(withText(context.getString(R.string.toast_region_deleted)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        //check that the downloaded list map is empty
        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }
            override fun onError(error: String) {} //left intentionally empty
        })
    }

    @Test
    fun canClickOnCancelDownloadDialog() {
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(NEGATIVE_BUTTON_ID)).perform(click())
    }

    @Test
    fun checkToastWhenMapHasEmptyName() {
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        onView(withText(context.getString(R.string.dialog_toast)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }
}
