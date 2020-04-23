package ch.epfl.sdp.ui.maps

import android.graphics.Color
import androidx.preference.PreferenceManager
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory

object MapUtils {

    const val DEFAULT_LATITUDE: Double = 47.39778846550371
    const val DEFAULT_LONGITUDE: Double = 8.545970150745575
    const val DEFAULT_ZOOM: Double = 9.0

    private val DARK_RED = Color.parseColor("#E55E5E")
    private val LIGHT_RED = Color.parseColor("#F9886C")
    private val ORANGE = Color.parseColor("#FBB03B")

    private fun loadLastMapPositionFromPrefs(): LatLng {
        val context = MainApplication.applicationContext()
        val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val latitude: Double = defaultSharedPrefs
                .getString(context.getString(R.string.prefs_latitude), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_LATITUDE
        val longitude: Double = defaultSharedPrefs
                .getString(context.getString(R.string.prefs_longitude), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_LONGITUDE
        return LatLng(latitude, longitude)
    }

    /**
     * Saves the camera position and zoom to the shared preferences
     */
    fun saveCameraPositionAndZoomToPrefs(cameraPosition: CameraPosition) {
        val context = MainApplication.applicationContext()
        PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext()).edit()
                .putString(context.getString(R.string.prefs_latitude),
                        cameraPosition.target?.latitude.toString())
                .putString(context.getString(R.string.prefs_longitude),
                        cameraPosition.target?.longitude.toString())
                .putString(context.getString(R.string.prefs_zoom),
                        cameraPosition.zoom.toString())
                .apply()
    }

    private fun loadLastMapZoomFromPrefs(): Double {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext())
                .getString(MainApplication.applicationContext().getString(R.string.prefs_zoom), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_ZOOM
    }

    /**
     * Returns the last camera state stored in the preferences
     */
    fun getLastCameraState(): CameraPosition {
        return CameraPosition.Builder()
                .target(loadLastMapPositionFromPrefs())
                .zoom(loadLastMapZoomFromPrefs())
                .build()
    }

    /**
     * Loads and applies the camera settings passed in parameters to the camera of the given map
     * @param mapboxMap
     * @param latLng
     * @param zoom
     */
    fun getCameraWithParameters(latLng: LatLng, zoom: Double): CameraPosition {
        return CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build()
    }

    /**
     * Creates and adds the layers necessary to display the heatmap information of the signal
     */
    fun createLayersForHeatMap(style: Style) {
        unclusteredLayerData(style)
        //clusteredLayerData(style)
    }

    private fun unclusteredLayerData(style: Style) {
        val unclustered = CircleLayer("unclustered-points",
                MainApplication.applicationContext().getString(R.string.heatmap_source_ID))
        unclustered.setProperties(
                PropertyFactory.circleColor(

                        Expression.interpolate(Expression.linear(),Expression.get("intensity"),
                                Expression.stop(8, Expression.rgb(0,0,255)),
                                Expression.stop(8.5, Expression.rgb(0,255,255)),
                                Expression.stop(9, Expression.rgb(0,255,0)),
                                Expression.stop(9.5, Expression.rgb(255,255,0)),
                                Expression.stop(10.0, Expression.rgb(255,0,0))
                        )
                ),
                PropertyFactory.circleRadius(40f),
                PropertyFactory.circleBlur(1.5f))
        unclustered.setFilter(Expression.neq(Expression.get("cluster"), Expression.literal(true)))
        style.addLayerBelow(unclustered,
                MainApplication.applicationContext().getString(R.string.heatmap_source_ID))
    }

    private fun clusteredLayerData(style: Style) {
        val layers = arrayOf(
                intArrayOf(15, DARK_RED),
                intArrayOf(10, LIGHT_RED),
                intArrayOf(2, ORANGE))
        layers.indices.forEach { i ->
            val circles = CircleLayer("cluster-$i",
                    MainApplication.applicationContext().getString(R.string.heatmap_source_ID))
            circles.setProperties(
                    PropertyFactory.circleColor(layers[i][1]),
                    PropertyFactory.circleRadius(100f),
                    PropertyFactory.circleBlur(1f)
            )

            val pointCount: Expression = Expression.toNumber(Expression.get("point_count"))
            circles.setFilter(Expression.gte(pointCount,Expression.literal(1)))
            circles.setFilter(
                    if (i == 0) Expression.gte(pointCount, Expression.literal(layers[i][0]))
                    else Expression.all(
                            Expression.gte(pointCount, Expression.literal(layers[i][0])),
                            Expression.lt(pointCount, Expression.literal(layers[i - 1][0]))
                    )
            )
            style.addLayerBelow(circles,
                    MainApplication.applicationContext().getString(R.string.heatmap_source_ID))
        }
    }
}