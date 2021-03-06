package ch.epfl.sdp.drone

import ch.epfl.sdp.searchareabuilder.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng

interface OverflightStrategy {

    fun acceptArea(searchArea: SearchArea): Boolean
    fun createFlightPath(startingPoint: LatLng, searchArea: SearchArea): List<LatLng>

}