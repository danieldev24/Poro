package com.apps.daniel.poro.domain.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query
import com.apps.daniel.poro.domain.models.Profile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfile(profile: Profile)

    @get:Query("select * from Profile")
    val profiles: LiveData<List<Profile>>

    @Query("delete from Profile where name = :name")
    fun deleteProfile(name: String)
}