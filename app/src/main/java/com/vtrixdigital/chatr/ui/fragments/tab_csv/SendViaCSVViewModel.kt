package com.vtrixdigital.chatr.ui.fragments.tab_csv

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.BulkImportList

class SendViaCSVViewModel : ViewModel() {

    private lateinit var db : AppDatabase
    var records : LiveData<List<BulkImportList>>? = null

    fun refresh(db: AppDatabase? , source : String){
        if (db != null) {
            this.db = db
            records =  db.bulkImportDao().getUniqueReceiver(source)
        }
    }
}