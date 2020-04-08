package ch.epfl.sdp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.doubleClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import ch.epfl.sdp.drone.Drone
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MapActivityTest {

    companion object {
        const val LATITUDE_TEST = "42.125"
        const val LONGITUDE_TEST = "-30.229"
        const val ZOOM_TEST = "0.9"
    }

    lateinit var preferencesEditor: SharedPreferences.Editor

    @get:Rule
    var mActivityRule = ActivityTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        val targetContext: Context = getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    @Test
    fun mapBoxCanAddMarker() {
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.mapView)).perform(click())
        Thread.sleep(1000)
        //click on the current marker once again to remove it
        onView(withId(R.id.mapView)).perform(click())
    }

    @Test
    fun mapBoxCanRemoveMarker() {
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.mapView)).perform(doubleClick())
    }

    @Test
    fun canStartMissionAndReturnHome() {
        // Launch activity
        mActivityRule.launchActivity(Intent())
        // Add 4 points to the map for the strategy
        runOnUiThread {
            arrayListOf(
                    LatLng(47.398979,  8.543434),
                    LatLng(47.398279, 8.543434),
                    LatLng(47.397426, 8.544867),
                    LatLng(47.397026, 8.543067)
            ).forEach { latLng -> mActivityRule.activity.onMapClicked(latLng) }
        }
        onView(withId(R.id.start_mission_button)).perform(click())
        Thread.sleep(10000)
        onView(withId(R.id.return_home)).perform(click())
    }

    @Test
    fun mapboxUseOurPreferences() {
        preferencesEditor
                .putString("latitude", LATITUDE_TEST)
                .putString("longitude", LONGITUDE_TEST)
                .putString("zoom", ZOOM_TEST)
                .apply()

        // Launch activity
        mActivityRule.launchActivity(Intent())

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assert(mapboxMap.cameraPosition.target.latitude.toString() == LATITUDE_TEST)
                assert(mapboxMap.cameraPosition.target.longitude.toString() == LONGITUDE_TEST)
                assert(mapboxMap.cameraPosition.zoom.toString() == ZOOM_TEST)
            }
        }
        runOnUiThread {
            Drone.currentPositionLiveData.postValue(LatLng(47.398039859999997, 8.5455725400000002))
        }
        getInstrumentation().waitForIdleSync()
        runOnUiThread {
            Drone.currentPositionLiveData.postValue(LatLng(47.398039859999997, 8.5455725400000002))
        }
        getInstrumentation().waitForIdleSync()
    }

    @Test
    fun mapBoxCanAddPointToHeatMap() {
        mActivityRule.launchActivity(Intent())
        getInstrumentation().waitForIdleSync()
        runOnUiThread {
            for (i in 0..30) {
                if (mActivityRule.activity.isMapboxMapInitialized()) break
                else Thread.sleep(100)
            }
            mActivityRule.activity.addPointToHeatMap(10.0, 10.0)
        }
    }

    @Test
    fun canUpdateUserLocation() {
        mActivityRule.launchActivity(Intent())
        CentralLocationManager.currentUserPosition.postValue(LatLng(LATITUDE_TEST.toDouble(), LONGITUDE_TEST.toDouble()))
    }

    @Test
    fun canUpdateUserLocationTwice() {
        mActivityRule.launchActivity(Intent())
        CentralLocationManager.currentUserPosition.postValue(LatLng(LATITUDE_TEST.toDouble(), LONGITUDE_TEST.toDouble()))
        CentralLocationManager.currentUserPosition.postValue(LatLng(-LATITUDE_TEST.toDouble(), -LONGITUDE_TEST.toDouble()))
    }

    @Test
    fun canOnRequestPermissionResult() {
        mActivityRule.launchActivity(Intent())
        mActivityRule.activity.onRequestPermissionsResult(1011, Array(0) { "" }, IntArray(0))
    }
}