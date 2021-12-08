package com.vtrixdigital.chatr.database

import android.content.Context

class DatabaseHelper {
    private var db : AppDatabase? = null
    fun getInstance(context : Context) : AppDatabase{
        return if(db == null){
            db = AppDatabase.getInstance(context)
            db as AppDatabase
        }else {
            db as AppDatabase
        }
    }
}