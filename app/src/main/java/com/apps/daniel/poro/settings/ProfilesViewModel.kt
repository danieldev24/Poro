package com.apps.daniel.poro.settings

import com.apps.daniel.poro.database.ProfileDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.daniel.poro.database.AppDatabase
import com.apps.daniel.poro.database.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(database: AppDatabase) : ViewModel() {

    private val dao: ProfileDao = database.profileDao()

    val profiles: LiveData<List<Profile>>
        get() = dao.profiles

    fun addProfile(profile: Profile) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.addProfile(profile)
        }
    }

    fun deleteProfile(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteProfile(name)
        }
    }
}