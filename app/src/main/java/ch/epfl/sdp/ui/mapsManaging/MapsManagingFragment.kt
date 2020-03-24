package ch.epfl.sdp.ui.mapsManaging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import ch.epfl.sdp.R
import ch.epfl.sdp.SavedMap
import kotlinx.android.synthetic.main.fragment_maps_managing.*

class MapsManagingFragment : Fragment() {

    private lateinit var mapsManagingViewModel: MapsManagingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mapsManagingViewModel =
                ViewModelProviders.of(this).get(MapsManagingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_maps_managing, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        mapsManagingViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = "Maps managing fragment\n(get offline maps here)"
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapNames: ArrayList<SavedMap> = ArrayList()
        mapNames.add(SavedMap("Switzerland"))
        mapNames.add(SavedMap("Germany"))
        mapNames.add(SavedMap("Italy"))
        mapNames.add(SavedMap("France"))
        mapNames.add(SavedMap("Spain"))
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView node initialized here
        mapSelectionRecyclerview.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(activity)
            // set the custom adapter to the RecyclerView
            adapter = MapSelectionRecyclerViewAdapter(mapNames)
        }
    }
}
