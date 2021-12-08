package com.vtrixdigital.chatr.ui.activities.live_chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.SentMessages

class LiveChatViewModel: ViewModel() {

    private lateinit var db : AppDatabase
    var records : LiveData<List<SentMessages>>? = null
    fun refresh(db: AppDatabase? , sender : String){
        if (db != null) {
            this.db = db
            records =  db.sentMessageDao().getFullChat(sender)
        }
    }
}