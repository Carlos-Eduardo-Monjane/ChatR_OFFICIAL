package com.vtrixdigital.chatr.ui.activities.bulk_data_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.BulkImportList

class BulkDataDetailsViewModel:ViewModel() {
    private lateinit var db : AppDatabase
    var records : LiveData<List<BulkImportList>>? = null

    fun refresh(db: AppDatabase? , campaignName: String, source: String){
        if (db != null) {
            this.db = db
            records =  db.bulkImportDao().getCampaignData(source , campaignName)
        }
    }
}