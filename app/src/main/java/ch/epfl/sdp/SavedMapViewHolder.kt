package ch.epfl.sdp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedMapViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.map_selection_recyclerview_row, parent, false)) {
    private var mTitleView: TextView? = null
    private var mYearView: TextView? = null


    init {
        mTitleView = itemView.findViewById(R.id.savedMapName)
    }

    fun bind(savedMap: SavedMap) {
        mTitleView?.text = savedMap.name
    }

}