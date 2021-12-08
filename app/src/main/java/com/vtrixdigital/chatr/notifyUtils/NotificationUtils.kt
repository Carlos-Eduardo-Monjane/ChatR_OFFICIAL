package com.vtrixdigital.chatr.notifyUtils

import android.app.Notification
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.vtrixdigital.chatr.models.Action
import java.util.*

object NotificationUtils {
    private val REPLY_KEYWORDS = arrayOf("reply", "android.intent.extra.text", "direct_reply_input")
    private val INPUT_KEYWORD: CharSequence = "input"
    fun getQuickReplyAction(n: Notification, packageName: String?): Action? {
        var action: NotificationCompat.Action? = null
        if (Build.VERSION.SDK_INT >= 24) action = getQuickReplyAction(n)
        if (action == null) action = getWearReplyAction(n)
        return if (action == null) null else Action(action, packageName, true)
    }

    private fun getQuickReplyAction(n: Notification): NotificationCompat.Action? {
        for (i in 0 until NotificationCompat.getActionCount(n)) {
            val action = NotificationCompat.getAction(n, i)
            if (action?.remoteInputs != null) {
                for (x in action.remoteInputs!!.indices) {
                    val remoteInput = action.remoteInputs!![x]
                    if (isKnownReplyKey(remoteInput.resultKey)) return action
                }
            }
        }
        return null
    }

    private fun getWearReplyAction(n: Notification): NotificationCompat.Action? {
        val wearableExtender = NotificationCompat.WearableExtender(n)
        for (action in wearableExtender.actions) {
            if (action.remoteInputs != null) {
                for (x in action.remoteInputs!!.indices) {
                    val remoteInput = action.remoteInputs!![x]
                    if (isKnownReplyKey(remoteInput.resultKey)) return action else if (remoteInput.resultKey.toLowerCase(
                            Locale.ROOT)
                            .contains(
                                INPUT_KEYWORD
                            )
                    ) return action
                }
            }
        }
        return null
    }

    private fun isKnownReplyKey(resultKey: String): Boolean {
        if (TextUtils.isEmpty(resultKey)) return false
        val resultKey1 = resultKey.toLowerCase(Locale.ROOT)
        for (keyword in REPLY_KEYWORDS) if (resultKey1.contains(keyword)) return true
        return false
    }
}