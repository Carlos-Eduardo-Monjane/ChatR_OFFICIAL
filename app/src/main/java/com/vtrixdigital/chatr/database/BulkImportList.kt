package com.vtrixdigital.chatr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bulkImportList")
data class BulkImportList(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "countryCode") val countryCode : String,
    @ColumnInfo(name = "receiver") val receiver : String,
    @ColumnInfo(name = "message") val message : String,
    @ColumnInfo(name = "isSent") val isSent : Boolean,
    @ColumnInfo(name = "appName") val appName : String,
    @ColumnInfo(name = "campaignName") val campaignName : String,
    @ColumnInfo(name = "source") val source : String,
    @ColumnInfo(name = "sentTime") val sentTime : String?,
    @ColumnInfo(name = "messageType") val messageType : String?,
    @ColumnInfo(name = "fileUri") val fileUri : String?,
    @ColumnInfo(name = "createdAt", defaultValue = "CURRENT_TIMESTAMP" ) val createdAt: String?
)