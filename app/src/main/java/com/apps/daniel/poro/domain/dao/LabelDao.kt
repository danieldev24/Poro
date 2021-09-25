package com.apps.daniel.poro.domain.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query
import com.apps.daniel.poro.domain.models.Label

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addLabel(label: Label)

    @get:Query("select * from Label where archived = 0 or archived = NULL ORDER BY `order`")
    val labels: LiveData<List<Label>>

    @get:Query("select * from Label ORDER BY `order`")
    val allLabels: LiveData<List<Label>>

    @Query("select colorId from Label where title = :title")
    fun getColor(title: String): LiveData<Int>

    @Query("update Label SET title = :newTitle WHERE title = :title")
    suspend fun editLabelName(title: String, newTitle: String?)

    @Query("update Label SET colorId = :colorId WHERE title = :title")
    suspend fun editLabelColor(title: String, colorId: Int)

    @Query("update Label SET `order` = :order WHERE title = :title")
    suspend fun editLabelOrder(title: String, order: Int)

    @Query("delete from Label where title = :title")
    suspend fun deleteLabel(title: String)

    @Query("update Label SET archived = :archived WHERE title = :title")
    suspend fun toggleLabelArchiveState(title: String, archived: Boolean)
}