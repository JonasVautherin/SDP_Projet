package ch.epfl.sdp.ui.maps

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.OfflineManagerActivity
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.json.JSONObject
import timber.log.Timber
import java.nio.charset.Charset


interface OfflineManagerUtils {

    // Progress bar methods
    fun startProgress(downloadButton: Button, listButton: Button, progressBar: ProgressBar) { // Disable buttons
        downloadButton.isEnabled = false
        listButton.isEnabled = false
        // Start and show the progress bar
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
    }

    fun endProgress(downloadButton: Button, listButton: Button, progressBar: ProgressBar) { // Don't notify more than once
        // Enable buttons
        downloadButton.isEnabled = true
        listButton.isEnabled = true
        // Stop and hide the progress bar
        progressBar.isIndeterminate = false
        progressBar.visibility = View.GONE
        // Show a toast
        showToast(MainApplication.applicationContext().getString(R.string.end_progress_success))
    }

    fun deleteOfflineRegion(offRegion: OfflineRegion, progressBar: ProgressBar) {
        offRegion.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
            override fun onDelete() { // Once the region is deleted, remove the
                // progressBar and display a toast
                progressBar.visibility = View.INVISIBLE
                progressBar.isIndeterminate = false
                showToast(MainApplication.applicationContext().getString(R.string.toast_region_deleted))
            }

            override fun onError(error: String) {
                progressBar.visibility = View.INVISIBLE
                progressBar.isIndeterminate = false
                Timber.e("Error: %s", error)
                showToast("Error : $error")
            }
        })
    }


    // Get the region name from the offline region metadata
    fun getRegionName(offlineRegion: OfflineRegion): String {
        val regionName: String
        regionName = try {
            JSONObject(String(offlineRegion.metadata, Charset.forName(OfflineManagerActivity.JSON_CHARSET)))
                    .getString(OfflineManagerActivity.JSON_FIELD_REGION_NAME)
        } catch (exception: Exception) {
            Timber.e("Failed to decode metadata: %s", exception.message)
            String.format(MainApplication.applicationContext().getString(R.string.region_name), offlineRegion.id)
        }
        return regionName
    }

    fun showToast(message : String){
        Toast.makeText(MainApplication.applicationContext(), message, Toast.LENGTH_LONG).show()
    }

}