package com.vtrixdigital.chatr.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BulkImportDAO {

    @Insert
    fun insertBulkImport(vararg messagesDAO: BulkImportList)

    @Query("select * from bulkImportList WHERE source = :source GROUP BY campaignName order by id desc")
    fun getUniqueReceiver(source : String): LiveData<List<BulkImportList>>

    @Query("select * from bulkImportList where source = :source and campaignName = :campaignName and isSent = :sentStatus")
    fun getPendingMessages(source : String , campaignName : String , sentStatus : Boolean): List<BulkImportList>

    @Query("select COUNT(id) as count from bulkImportList where isSent = :sentStatus")
    fun getSentMessages(sentStatus: Boolean): LiveData<Int>

    @Query("select COUNT(id) as count from bulkImportList where isSent = :sentStatus AND createdAt >= :dateStart AND createdAt <= :dateEnd")
    fun getTodaySentCount(dateStart: String, dateEnd: String, sentStatus: Boolean): Int

    @Query("select COUNT(id) from bulkImportList where source = :source GROUP BY campaignName")
    fun getTotalImportedViaSource(source: String):LiveData<Int>

    @Query("UPDATE bulkImportList SET isSent = :status,sentTime=:sentTime WHERE id IN(:id)")
    fun updateSentStatus(status: Boolean, id: ArrayList<Int>, sentTime: String)

    @Query("DELETE FROM  bulkImportList WHERE campaignName = :campaignName AND source =:source AND appName =:appName")
    fun deleteUploadedQueue(campaignName: String, source: String,appName: String)

    @Query("DELETE FROM  bulkImportList WHERE id = :id")
    fun deleteQueueById(id: Int)

    @Query("SELECT * FROM  bulkImportList WHERE campaignName = :campaignName LIMIT 1")
    fun getCampaignByName(campaignName: String):BulkImportList

    @Query("select * from bulkImportList WHERE source = :source AND campaignName = :campaignName order by id desc")
    fun getCampaignData(source : String, campaignName: String): LiveData<List<BulkImportList>>
}