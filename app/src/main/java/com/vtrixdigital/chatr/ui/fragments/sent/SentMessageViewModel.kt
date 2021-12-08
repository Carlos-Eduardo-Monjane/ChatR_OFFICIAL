package com.vtrixdigital.chatr.ui.fragments.sent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.SentMessages

class SentMessageViewModel: ViewModel() {

    private lateinit var db : AppDatabase
    var records : LiveData<List<SentMessages>>? = null

    fun refresh(db: AppDatabase?){
        if (db != null) {
            this.db = db
            records =  db.sentMessageDao().getUniqueSender()
        }
    }

    fun filterRecords(searchString : String) {
        records = db.sentMessageDao().filterUniqueSender(searchString)
    }
}