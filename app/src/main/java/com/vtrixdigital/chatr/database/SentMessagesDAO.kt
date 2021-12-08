package com.vtrixdigital.chatr.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
// OR receivedMessage LIKE :searchString OR replyMessage LIKE :searchString
interface SentMessagesDAO {

    @Query("select * from SentMessages GROUP BY sender order by id desc")
    fun getUniqueSender(): LiveData<List<SentMessages>>

    @Query("select * from SentMessages WHERE sender = :sender ORDER BY id DESC")
    fun getFullChat(sender: String): LiveData<List<SentMessages>>

    @Query("select * from SentMessages WHERE sender LIKE :searchString order by id desc")
    fun filterUniqueSender(searchString: String): LiveData<List<SentMessages>>

    @Query("SELECT COUNT(id) as count from SentMessages")
    fun getReceivedCount(): LiveData<Int>

    @Query("SELECT COUNT(id) as count from SentMessages WHERE createdAt >= :dateStart AND createdAt <= :dateEnd")
    fun getSentCount(dateStart:String , dateEnd:String): Int

    @Insert
    fun insertSentMessage(vararg messagesDAO: SentMessages)
}