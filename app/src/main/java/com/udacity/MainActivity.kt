package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var action: NotificationCompat.Action

    private val downloadMap = mutableMapOf<Long, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        setupNotificationChannel()

        custom_button.setOnClickListener {
            val option = options_group.checkedRadioButtonId
            if (option == -1) {
                Toast.makeText(this, R.string.no_option_selected_toast_message, Toast.LENGTH_SHORT)
                    .show()
            } else {
                val optionInfos = URLS[option]
                optionInfos?.let {
                    val downloadId = download(getString(it.first))
                    downloadMap.put(downloadId, it.second)
                }
            }
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(CHANNEL_NAME),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(CHANNEL_DESCRIPTION)

            notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadInfo = downloadMap.remove(id)

            downloadInfo?.let {
                val contentIntent = Intent(applicationContext, DetailActivity::class.java)
                contentIntent.putExtra(DetailActivity.EXTRA_FILE_NAME, downloadInfo)
                contentIntent.putExtra(DetailActivity.EXTRA_DOWNLOAD_ID, id)

                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    NOTIFICATION_ID,
                    contentIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )

                val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_description))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(
                        android.R.drawable.ic_dialog_info,
                        getString(R.string.notification_button),
                        pendingIntent
                    )

                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun download(url: String): Long {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private val URLS = mapOf(
            R.id.option_glide to (R.string.glide_url to R.string.glide_info),
            R.id.option_loadapp to (R.string.loadapp_url to R.string.loadapp_info),
            R.id.option_retrofit to (R.string.retrofit_url to R.string.retrofit_info)
        )

        private const val NOTIFICATION_ID = 0xCAFE

        private const val CHANNEL_ID = "download_notifications"
        private const val CHANNEL_NAME = R.string.channel_name
        private const val CHANNEL_DESCRIPTION = R.string.channel_description
    }

}
