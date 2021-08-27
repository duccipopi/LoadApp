package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // Dismiss the notification
        getSystemService(NotificationManager::class.java).cancelAll()

        // Get information from the notification intent
        var filename = R.string.no_filename_defined
        var downloadId = -1L
        intent.extras?.let {
            filename = it.getInt(EXTRA_FILE_NAME, R.string.no_filename_defined)
            downloadId = it.getLong(EXTRA_DOWNLOAD_ID)
        }

        // Check download status
        val downloadManager = getSystemService(DownloadManager::class.java) as DownloadManager
        val request = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        var success = false
        if (request.moveToFirst())
            success =
                request.getInt(request.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL

        // Set text and color based in download status
        filename_text.text = getString(filename)
        status_text.text =
            getString(if (success) R.string.download_success else R.string.download_failed)

        val color = getColor(if (success) R.color.colorSuccess else R.color.colorFailed)
        cloud_status.setColorFilter(color)
        status_text.setTextColor(color)

        close_button.setOnClickListener {
            finish()
        }


    }


    companion object {
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_DOWNLOAD_ID = "download_id"
    }

}
