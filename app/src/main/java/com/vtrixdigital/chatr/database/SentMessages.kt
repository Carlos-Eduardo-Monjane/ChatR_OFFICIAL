package com.vtrixdigital.chatr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SentMessages")
data class SentMessages(
        @PrimaryKey val id: Int?,
        @ColumnInfo(name = "sender") val sender : String,
        @ColumnInfo(name = "receivedMessage") val receivedMessage : String,
        @ColumnInfo(name = "replyMessage") val replyMessage : String,
        @ColumnInfo(name = "isGroupMessage") val isGroupMessage : Boolean,
        @ColumnInfo(name = "groupName") val groupName : String?,
        @ColumnInfo(name = "appName") val appName : String,
        @ColumnInfo(name = "activatedRuleId") val activatedRuleId : String,
        @ColumnInfo(name = "createdAt", defaultValue = "CURRENT_TIMESTAMP" ) val createdAt: String?
    )