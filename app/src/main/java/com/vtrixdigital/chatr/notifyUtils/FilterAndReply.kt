package com.vtrixdigital.chatr.notifyUtils

import android.annotation.SuppressLint
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.api.APIService
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.database.Rules
import com.vtrixdigital.chatr.database.SentMessages
import com.vtrixdigital.chatr.models.Action
import com.vtrixdigital.chatr.ui.activities.MainActivity
import com.vtrixdigital.chatr.utils.Constants
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*

class FilterAndReply {
    private val tags: String = "FilterAndReply"
    fun sendMessage(
        context: Context,
        action: Action,
        receivedMessage: String,
        from: String,
        headerMessage: String?,
        footerMessage: String?,
        isGroupMessage: Boolean,
        groupName: String?,
        packageName: String,
        delay_reply: String?,
        notification_settings: Boolean,
        dailyLimit: Int?
    ) {
        GlobalScope.launch {
            var dailyLimitInt = Constants().getAutoReplyLimit()
            if(dailyLimit != null){
                dailyLimitInt = dailyLimit.toInt()
            }
            val db = context.let { DatabaseHelper().getInstance(it) }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()
            val sentToday = db.sentMessageDao().getSentCount("$today 00:00:00" , "$today 23:59:59")
            if(sentToday < dailyLimitInt) {
                Log.d(tags, receivedMessage)
                val rule: Rules? = db.rulesDao().getReplyRule(receivedMessage, "exactly")
                if (rule != null) {
                    Log.d(tags, "NOT NULL RULE")
                    doReply(
                        action,
                        context,
                        rule,
                        receivedMessage,
                        from,
                        headerMessage,
                        footerMessage,
                        isGroupMessage,
                        groupName,
                        packageName,
                        db,
                        delay_reply,
                        notification_settings
                    )
                } else {
                    val rules: List<Rules> = db.rulesDao().getAllSimilarlyMatchedRules()
                    if (rules.isNotEmpty()) {
                        Log.d(tags, "NOT Empty RULES")
                        for (currentRule in rules) {
                            if (currentRule.incoming_message.contains("*", true)) {
                                if (currentRule.incoming_message.length == 1) {
                                    doReply(
                                        action,
                                        context,
                                        currentRule,
                                        receivedMessage,
                                        from,
                                        headerMessage,
                                        footerMessage,
                                        isGroupMessage,
                                        groupName,
                                        packageName,
                                        db,
                                        delay_reply,
                                        notification_settings
                                    )
                                    break
                                } else {
//                                val savedIncomingMessage = currentRule.incoming_message.replace("*", "")
                                    val s = currentRule.incoming_message.substringBefore("*")
                                    val e = currentRule.incoming_message.substringAfter("*")
                                    if (currentRule.incoming_message.startsWith("*")) {
                                        if (receivedMessage.endsWith(e, true)) {
                                            doReply(
                                                action,
                                                context,
                                                currentRule,
                                                receivedMessage,
                                                from,
                                                headerMessage,
                                                footerMessage,
                                                isGroupMessage,
                                                groupName,
                                                packageName,
                                                db,
                                                delay_reply,
                                                notification_settings
                                            )
                                            break
                                        }
                                    } else if (currentRule.incoming_message.endsWith("*")) {
                                        if (receivedMessage.startsWith(s, true)) {
                                            doReply(
                                                action,
                                                context,
                                                currentRule,
                                                receivedMessage,
                                                from,
                                                headerMessage,
                                                footerMessage,
                                                isGroupMessage,
                                                groupName,
                                                packageName,
                                                db,
                                                delay_reply,
                                                notification_settings
                                            )
                                            break
                                        }
                                    } else {
                                        if (receivedMessage.startsWith(
                                                s,
                                                true
                                            ) && receivedMessage.endsWith(e, true)
                                        ) {
                                            doReply(
                                                action,
                                                context,
                                                currentRule,
                                                receivedMessage,
                                                from,
                                                headerMessage,
                                                footerMessage,
                                                isGroupMessage,
                                                groupName,
                                                packageName,
                                                db,
                                                delay_reply,
                                                notification_settings
                                            )
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d(tags, "Empty RULES")
                    }
                }
            }else{
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context , "Daily limit reached!" , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun doReply(
        action: Action,
        context: Context,
        rule: Rules,
        receivedMessage: String,
        from: String,
        headerMessage: String?,
        footerMessage: String?,
        isGroupMessage: Boolean,
        groupName: String?,
        packageName: String,
        db: AppDatabase,
        delay_reply: String?,
        notification_settings: Boolean
    ) {

        if (!delay_reply.isNullOrEmpty() && delay_reply != "0") {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                finallyReply(
                    action,
                    context,
                    rule,
                    receivedMessage,
                    from,
                    headerMessage,
                    footerMessage,
                    isGroupMessage,
                    groupName,
                    packageName,
                    db,
                    notification_settings
                )
            }, 1000 * delay_reply.toLong())
        } else {
            finallyReply(
                action,
                context,
                rule,
                receivedMessage,
                from,
                headerMessage,
                footerMessage,
                isGroupMessage,
                groupName,
                packageName,
                db,
                notification_settings
            )
        }
    }


    private fun finallyReply(
        action: Action,
        context: Context,
        rule: Rules,
        receivedMessage: String,
        from: String,
        headerMessage: String?,
        footerMessage: String?,
        isGroupMessage: Boolean,
        groupName: String?,
        packageName: String,
        db: AppDatabase,
        notification_settings: Boolean
    ) {
        if (!rule.isServerReply) {
            Log.d(tags , "NO server reply")
            var replyMessage: String = rule.reply_message

            if(rule.reply_message.contains("{FIRST_NAME}")){
                val parts : List<String> = from.split(" ")
                replyMessage = replyMessage.replace("{FIRST_NAME}" , parts[0])
            }

            if(rule.reply_message.contains("{LAST_NAME}")){
                val parts : List<String> = from.split(" ")
                replyMessage = if(parts.size > 1)
                    replyMessage.replace("{LAST_NAME}" , parts[(parts.size -1)])
                else
                    replyMessage.replace("{LAST_NAME}" , "")
            }

            if(rule.reply_message.contains("{DATE}")){
                val currentDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                replyMessage  = replyMessage.replace("{DATE}" , currentDate)
            }

            if(rule.reply_message.contains("{TIME}")){
                val currentDate: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                replyMessage = replyMessage.replace("{TIME}" , currentDate)
            }

            if (headerMessage != null  && headerMessage.isNotEmpty())
                replyMessage = "$headerMessage\n$replyMessage"

            if (footerMessage != null && footerMessage.isNotEmpty())
                replyMessage = "$replyMessage\n$footerMessage"

            try {
                action.sendReply(context, replyMessage)
                addLog(
                    rule.id.toString(),
                    from,
                    isGroupMessage,
                    groupName,
                    packageName,
                    receivedMessage,
                    replyMessage,
                    db,
                    notification_settings,
                    context
                )
            } catch (e: PendingIntent.CanceledException) {
                Log.d(tags, "PendingIntent.CanceledException ${e.message}")
            }

        } else {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://google.com/")
                .build()

            val service = retrofit.create(APIService::class.java)

            val jsonObject = JSONObject()
            jsonObject.put("app_name", packageName)
            jsonObject.put("message", receivedMessage)
            jsonObject.put("sender", from)

            val jsonObjectString = jsonObject.toString()
            val requestBody =
                jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
               if (!rule.headerKey.isNullOrBlank() && !rule.headerValue.isNullOrBlank()) {
                    val myMap: Map<String, String> =
                        mapOf(rule.headerKey.toString() to rule.headerValue.toString())
                    val response: Response<ResponseBody> =
                        service.testApiWithHeaders(rule.serverUrl, myMap, requestBody)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            response.body()?.string()?.let { Log.d(tags, it) }
                            if (response.headers()["Content-Type"] != null) {
                                if (response.body()?.contentType()
                                        .toString() == "application/json"
                                ) {
                                    try {
                                        val json = JSONObject(response.body()?.string()!!)
                                        val reply = json.getString("reply")
                                        if (reply != "") {
                                            try {
                                                action.sendReply(context, reply)
                                                addLog(
                                                    rule.id.toString(),
                                                    from,
                                                    isGroupMessage,
                                                    groupName,
                                                    packageName,
                                                    receivedMessage,
                                                    reply,
                                                    db,
                                                    notification_settings,
                                                    context
                                                )
                                            } catch (e: PendingIntent.CanceledException) {
                                                Log.d(
                                                    tags,
                                                    "PendingIntent.CanceledException $e"
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {

                                    }
                                }
                            }
                        }
                    }
                } else {
                    val response: Response<ResponseBody> =
                        service.testApiWithOutHeaders(rule.serverUrl, requestBody)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d(tags, response.body().toString())
                            if (response.headers()["Content-Type"] != null) {
                                if (response.body()?.contentType()
                                        .toString() == "application/json"
                                ) {
                                    try {
                                        val json = JSONObject(response.body()?.string()!!)
                                        val reply = json.getString("reply")
                                        if (reply != "") {
                                            try {
                                                action.sendReply(context, reply)
                                                addLog(
                                                    rule.id.toString(),
                                                    from,
                                                    isGroupMessage,
                                                    groupName,
                                                    packageName,
                                                    receivedMessage,
                                                    reply,
                                                    db,
                                                    notification_settings,
                                                    context
                                                )
                                            } catch (e: PendingIntent.CanceledException) {
                                                Log.d(
                                                    tags,
                                                    "PendingIntent.CanceledException $e"
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addLog(
        ruleId: String,
        from: String,
        isGroupMessage: Boolean,
        groupName: String?,
        packageName: String,
        receivedMessage: String,
        replyMessage: String,
        db: AppDatabase,
        notification_settings: Boolean,
        context: Context
    ) {
        db.sentMessageDao().insertSentMessage(
            SentMessages(
                null,
                from,
                receivedMessage,
                replyMessage,
                isGroupMessage,
                groupName,
                packageName,
                ruleId,
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
        )
        if (notification_settings) {
            notifyUserForReply(
                context,
                from,
                receivedMessage,
            )
        }
    }

    @SuppressLint("WrongConstant")
    private fun notifyUserForReply(
        context: Context,
        from: String,
        receivedMessage: String
    ) {
        val textTitle = "New Reply Sent!"
        val textContent = "A new reply sent to $from for received message '$receivedMessage'"
        val channelId = (1111111..99999999).random().toString()
        val notificationId = (1111111..99999999).random()
        val groupKeyWorkEmail = "com.autoReply.forWA.MultipleMessage"

        createNotificationChannel(context, channelId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.dummy_icon)
            .setContentTitle(textTitle)
            .setVisibility(VISIBILITY_PUBLIC)
            .setGroup(groupKeyWorkEmail)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context , CHANNEL_ID : String) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AutoReply Notification"
            val descriptionText = "You will get notification whenever a reply is sent. You can turn it off from settings of app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
