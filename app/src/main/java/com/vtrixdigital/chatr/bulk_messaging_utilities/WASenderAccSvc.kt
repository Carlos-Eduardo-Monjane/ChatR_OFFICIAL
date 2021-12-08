package com.vtrixdigital.chatr.bulk_messaging_utilities

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.annotation.RequiresApi

class WASenderAccSvc : AccessibilityService() {
    private var privateMode = 0
    private val prefName = "my-custom-auto-reply-preferences"
    private lateinit var sharedPref: SharedPreferences
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPref = applicationContext.getSharedPreferences(prefName, privateMode)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        sharedPref = applicationContext.getSharedPreferences(prefName, privateMode)
        super.onCreate()
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!sharedPref.getBoolean("running" , false)) {
            return
        }

        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.eventType) {
            val activityName = event.className.toString()
            val messageType = sharedPref.getString("messageType", "")
            val packageName = sharedPref.getString("packageName" , "")
            Log.d("activityName" , "activityName : $activityName")
            Log.d("activityName" , "messageType : $messageType")
            if(messageType == "text" || messageType == null || messageType == ""){
                when (activityName) {
                    "$packageName.Conversation" -> {
                        val nodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId(sharedPref.getString("packageName" , "") + ":id/send")
                        if (nodes.size > 0) {
                            nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                    "$packageName.HomeActivity" -> {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        sendNext()
                    }
                }
            }else{
                when (activityName) {
                    "$packageName.mediacomposer.MediaComposerActivity" -> {
                        val nodes = rootInActiveWindow.findAccessibilityNodeInfosByText("send")
                        if (nodes.size > 0) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                performGlobalAction(GLOBAL_ACTION_BACK)
                                sendNext()
                            } , 500)
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Toast.makeText(
            this,
            "Please allow accessibility permission to WhatsApp Sender",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun sendNext() {
        try {
            val intent = Intent(this, WASenderFgSvc::class.java)
            intent.putExtra("start", true)
            intent.putExtra("source", sharedPref.getString("source", ""))
            intent.putExtra("campaignName", sharedPref.getString("campaignName", ""))
            intent.putExtra("messageType", sharedPref.getString("messageType", ""))
            startService(intent)
        }catch (e:Exception){
            Toast.makeText(
                this,
                "Unable to start service again!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}