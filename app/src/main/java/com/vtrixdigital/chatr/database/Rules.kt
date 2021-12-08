package com.vtrixdigital.chatr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Rules")
data class Rules(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(name = "incoming_message") val incoming_message: String,
    @ColumnInfo(name = "reply_message") val reply_message: String,
    @ColumnInfo(name = "matchType") val matchType: String,
    @ColumnInfo(name = "isServerReply") val isServerReply: Boolean,
    @ColumnInfo(name = "serverUrl") val serverUrl: String,
    @ColumnInfo(name = "headerKey") val headerKey: String?,
    @ColumnInfo(name = "headerValue") val headerValue: String?)