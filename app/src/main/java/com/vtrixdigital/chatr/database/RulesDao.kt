package com.vtrixdigital.chatr.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RulesDao {
    @Query("select * from Rules order by id desc")
    fun getAllRules(): List<Rules>

    @Query("select * from Rules WHERE id=:id")
    fun getRuleById(id: Int) : Rules

    @Query("select * from Rules WHERE incoming_message=:incoming_message")
    fun getRuleByIncomingMessage(incoming_message: String) :List<Rules>

    @Insert
    fun insertRule(vararg rulesDao: Rules)

    @Update
    fun updateRule(vararg rulesDao: Rules)

    @Query("DELETE FROM Rules WHERE id = :id")
    fun deleteRuleById(id: Int)

    @Query("SELECT * FROM Rules WHERE incoming_message LIKE:message AND matchType = :matchType")
    fun getReplyRule(message : String, matchType : String) : Rules?

    @Query("select * from Rules WHERE matchType = 'similarly' order by id desc")
    fun getAllSimilarlyMatchedRules(): List<Rules>

}