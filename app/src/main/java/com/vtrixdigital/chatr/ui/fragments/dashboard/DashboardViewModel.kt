package com.vtrixdigital.chatr.ui.fragments.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vtrixdigital.chatr.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private lateinit var db : AppDatabase
    var sent : LiveData<Int> = MutableLiveData(0)
    var received : LiveData<Int> = MutableLiveData(0)
    var contactsImported : LiveData<Int> = MutableLiveData(0)
    var csvImported : LiveData<Int> = MutableLiveData(0)

    fun refresh(db: AppDatabase? , csvSource: String , contactsSource : String){
        if (db != null) {
            this.db = db
            CoroutineScope(Dispatchers.IO).launch {
                received = db.sentMessageDao().getReceivedCount()
                sent = db.bulkImportDao().getSentMessages(true)
                contactsImported = db.bulkImportDao().getTotalImportedViaSource(contactsSource)
                csvImported = db.bulkImportDao().getTotalImportedViaSource(csvSource)
            }
        }
    }
}