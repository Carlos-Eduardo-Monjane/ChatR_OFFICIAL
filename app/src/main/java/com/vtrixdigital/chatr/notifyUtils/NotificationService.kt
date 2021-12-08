package com.vtrixdigital.chatr.notifyUtils

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.content.SharedPreferences
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.preference.PreferenceManager
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.notifyUtils.NotificationUtils.getQuickReplyAction
import com.vtrixdigital.chatr.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("OverrideAbstract")
class NotificationService : NotificationListenerService() {
    private var sharedPref: SharedPreferences? = null
    private var groupName: String? = null
    private var isGroupMessage = false
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
//        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            return
        }

        //is reply settings on
        sharedPref = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        val isAllowed = sharedPref?.getBoolean("replySettings", true)
        if (!isAllowed!!) {
            return
        }

        //is this package allowed
        if (!sharedPref?.getBoolean(sbn.packageName, false)!!) {
//            Log.d(TAG, "PACKAGE NOT ALLOWED")
            return
        }

        //is this from you
        val from = sbn.notification.extras.getString("android.title") ?: return
        if (from == "You") {
            return
        }

        //validation with numeric is required and pending
        if (from.contains("91") && sharedPref?.getBoolean("reply_to_saved_no_only", false)!!) {
            return
        }

        //is group reply allowed
        if (sbn.notification.extras.getBoolean("android.isGroupConversation")) {
            if (!sharedPref?.getBoolean("reply_to_groups", true)!!) {
                return
            }
            isGroupMessage = true
            groupName = sbn.notification.extras.getString("android.hiddenConversationTitle")
        }

        //is sending messages today allowed
        val weekdayName =
            SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis())
                .toLowerCase(Locale.ROOT)
        if (!sharedPref?.getBoolean(weekdayName, true)!!) {
            return
        }
        val action = getQuickReplyAction(sbn.notification, packageName)
        if (action != null) {
            val headerMessage = sharedPref?.getString("header_message", null)
            val footerMessage = sharedPref?.getString("footer_message", null)
            var msg = sbn.notification.extras.getString("android.text")
            if (msg != null && !msg.equals("📷 Photo", ignoreCase = true)) {
                if(sbn.packageName == getString(R.string.keyInstagram)){
                    msg = msg.substringAfterLast(": ")
                }
                FilterAndReply().sendMessage(
                    applicationContext,
                    action,
                    msg,
                    from,
                    headerMessage,
                    footerMessage,
                    isGroupMessage,
                    groupName,
                    sbn.packageName,
                    sharedPref?.getString("delay_reply", null),
                    sharedPref?.getBoolean("notification_settings", true)!!,
                    Constants().getAutoReplyLimit()
                )
            }
        }
    }
}