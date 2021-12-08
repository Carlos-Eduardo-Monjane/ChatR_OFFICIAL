package com.vtrixdigital.chatr.bulk_messaging_utilities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.BulkImportList
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class WASenderFgSvc : Service() {
    private var totalMessages = 0
    private var currentRecord = 0
    private var sentToday = 0
    private var records: List<BulkImportList> = arrayListOf()
    private lateinit var db: AppDatabase
    private var processedRecords = arrayListOf<Int>()
    private var privateMode = 0
    private val prefName = "my-custom-auto-reply-preferences"
    private lateinit var sharedPref: SharedPreferences
    private var sharedPrefDefault: SharedPreferences? = null
    private var messageLimit = Constants().getBulkMessagingLimit()
    private val notificationChannelId = "com.auto-reply.for-wa"
    private val whatsappPostFix = "@s.whatsapp.net"
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        sharedPref = applicationContext.getSharedPreferences(prefName, privateMode)
        val start = intent.getBooleanExtra("running", true)
        val source = intent.getStringExtra("source").toString()
        val campaignName = intent.getStringExtra("campaignName").toString()
        val messageType = intent.getStringExtra("messageType").toString()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()
        if (start) {
            sharedPrefDefault = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            if (sharedPrefDefault != null) {
                sharedPrefDefault?.getString("bulk_message_daily_limit", null)?.let {
                    messageLimit = it.toInt()
                }
            }
            if (messageLimit <= sentToday) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        applicationContext,
                        "Daily limit reached!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (isPermissionGranted(applicationContext)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startMyOwnForeground()
                    } else {
                        val notificationBuilder = Notification.Builder(this)
                        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                        notificationBuilder.setContentText("App Running in Background")
                        val not = notificationBuilder.build()
                        startForeground(NOTIFICATION_ID, not)
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        db = DatabaseHelper().getInstance(applicationContext)
                        records = db.bulkImportDao().getPendingMessages(source, campaignName, false)
                        totalMessages = records.size
                        sharedPref.edit().putBoolean("running", true).apply()
                        sharedPref.edit().putString("source", source).apply()
                        sharedPref.edit().putString("campaignName", campaignName).apply()
                        sharedPref.edit().putString("messageType", messageType).apply()
                        sentToday = db.bulkImportDao()
                            .getTodaySentCount("$today 00:00:00", "$today 23:59:59", true)
                        if (totalMessages > 0)
                            send()
                        else {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    applicationContext,
                                    "No messages remaining to send!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            applicationContext,
                            "Accessibility permission not allowed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun isPermissionGranted(mContext: Context): Boolean {
        return true
        /*var accessibilityEnabled = 0
        val service = packageName + "/" + WASenderAccSvc::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {

        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

        if (accessibilityEnabled == 1) {
            val settingValue: String = Settings.Secure.getString(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                if (accessibilityService.equals(service, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false*/
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("ApplySharedPref")
    private fun send() {
        if (currentRecord >= totalMessages) {
            sharedPref.edit().putBoolean("running", false).apply()
            sharedPref.edit().putString("packageName", null).apply()
            sharedPref.edit().putString("source", null).apply()
            sharedPref.edit().putString("campaignName", null).apply()
            sharedPref.edit().putString("messageType", null).apply()
            stopFgService()
            updateSentStatus()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "Task Completed", Toast.LENGTH_SHORT).show()
            }
            return
        } else if (messageLimit <= sentToday) {
            sharedPref.edit().putBoolean("running", false).apply()
            sharedPref.edit().putString("packageName", null).apply()
            sharedPref.edit().putString("source", null).apply()
            sharedPref.edit().putString("campaignName", null).apply()
            sharedPref.edit().putString("messageType", null).apply()
            stopFgService()
            updateSentStatus()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "Daily limit reached", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val recipient = "${records[currentRecord].countryCode}${records[currentRecord].receiver}"
        val message = records[currentRecord].message

        updateSentStatus()
        Handler(Looper.getMainLooper()).post {
            try {
                var packageName = records[currentRecord].appName
                val messageType = records[currentRecord].messageType
                if (packageName == "0" || packageName == "") {
                    packageName = getString(R.string.whatsapp_package)
                }
                val pm = this.packageManager
                sharedPref.edit().putString("packageName", packageName).apply()
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)

                var shareIntent : Intent? = null
                if( messageType == "text" || messageType == "" || messageType.isNullOrBlank()){
                    shareIntent = Intent(Intent.ACTION_VIEW)
                    val url = "https://wa.me/$recipient" + "?text=" + URLEncoder.encode(message, "utf-8")
                    shareIntent.setPackage(packageName)
                    shareIntent.data = Uri.parse(url)
                    shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }else if(messageType == "image"){
                    records[currentRecord].fileUri?.let {
                        shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "images/*"
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(records[currentRecord].fileUri))
                            putExtra("jid", recipient + whatsappPostFix)//phone number without "+" prefix
                            setPackage(packageName)
                        }
                    }
                }else if(messageType == "video"){
                    records[currentRecord].fileUri?.let {
                        shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(records[currentRecord].fileUri))
                            putExtra("jid", recipient + whatsappPostFix)//phone number without "+" prefix
                            setPackage(packageName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    }
                }else if(messageType == "document"){
                    records[currentRecord].fileUri?.let {
                        shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(records[currentRecord].fileUri))
                            putExtra("jid", recipient + whatsappPostFix)//phone number without "+" prefix
                            setPackage(packageName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    }
                }

                if (shareIntent?.resolveActivity(packageManager) != null) {
                    records[currentRecord].id?.let { processedRecords.add(it) }
                    currentRecord++
                    sentToday++
                    startActivity(shareIntent)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this,
                            "Whatsapp app not installed in your phone1",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        stopFgService()
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this,
                        "Whatsapp app not installed in your phone",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    stopFgService()
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Error : ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    stopFgService()
                }
            }
        }
    }

    private fun updateSentStatus() {
        GlobalScope.launch(Dispatchers.IO) {
            db.bulkImportDao().updateSentStatus(
                true,
                processedRecords,
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    .toString()
            )
        }
    }

    private fun stopFgService() {
        val intent = Intent(this, WASenderFgSvc::class.java)
        stopService(intent)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val channelName = "Background Service"
        val channel = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(channel)
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("App Running in Background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 12
    }
}