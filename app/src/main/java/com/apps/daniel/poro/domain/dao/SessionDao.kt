package com.apps.daniel.poro.domain.dao

import androidx.room.Dao
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apps.daniel.poro.domain.models.Session
import com.apps.daniel.poro.util.startOfTodayMillis
import com.apps.daniel.poro.util.startOfTomorrowMillis

@Dao
interface SessionDao {
    @Query("select * from Session where id = :id")
    fun getSession(id: Long): LiveData<Session>

    @get:Query("select * from Session ORDER BY timestamp DESC")
    val allSessions: LiveData<List<Session>>

    @get:Query("select * from Session where archived is 0 OR archived is NULL ORDER BY timestamp DESC")
    val allSessionsUnarchived: LiveData<List<Session>>

    @Query("select * from Session where archived is 0 OR archived is NULL and timestamp >= :startOfToday and timestamp < :startOfTomorrow ORDER BY timestamp DESC")
    fun getAllSessionsUnarchivedToday(startOfToday : Long = startOfTodayMillis(), startOfTomorrow : Long = startOfTomorrowMillis()): LiveData<List<Session>>

    @get:Query("select * from Session where label is NULL ORDER BY timestamp DESC")
    val allSessionsUnlabeled: LiveData<List<Session>>

    @Query("select * from Session where label = :label ORDER BY timestamp DESC")
    fun getSessions(label: String): LiveData<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSession(session: Session)

    @Query("update Session SET timestamp = :timestamp, duration = :duration, label = :label WHERE id = :id")
    suspend fun editSession(id: Long, timestamp: Long, duration: Int, label: String?)

    @Query("update Session SET label = :label WHERE id = :id")
    suspend fun editLabel(id: Long, label: String?)

    @Query("delete from Session where id = :id")
    suspend fun deleteSession(id: Long)

    /**
     * Deletes sessions finished at a later timestamp than the one provided as input.
     * Typically used to delete today's finished sessions
     * @param timestamp Sessions finished later than this timestamp will be deleted
     */
    @Query("delete from Session where timestamp >= :timestamp")
    suspend fun deleteSessionsAfter(timestamp: Long)

    @Query("delete from Session")
    suspend fun deleteAllSessions()
}