package ch.epfl.sdp.map

import ch.epfl.sdp.searchareabuilder.PolygonArea
import ch.epfl.sdp.searchareabuilder.SearchArea
import ch.epfl.sdp.searchareabuilder.SearchAreaNotCompleteException
import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonBuilder : SearchAreaBuilder() {

    override fun addVertex(vertex: LatLng): SearchAreaBuilder {
        vertices.add(vertex)
        this.vertices = this.vertices
        return this
    }

    override fun moveVertex(old: LatLng, new: LatLng): SearchAreaBuilder {
        val oldIndex = vertices.withIndex().minBy { it.value.distanceTo(old) }?.index
        vertices.removeAt(oldIndex!!)
        vertices.add(new)
        this.vertices = this.vertices
        return this
    }

    override fun isComplete(): Boolean {
        return vertices.size >= 3
    }

    override fun build(): SearchArea {
        if (!isComplete()) {
            throw SearchAreaNotCompleteException("Quarilateral not complete")
        }
        return PolygonArea(vertices)
    }
}