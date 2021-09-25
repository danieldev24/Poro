
package com.apps.daniel.poro.statistics.all_sessions

import androidx.lifecycle.ViewModel
import com.apps.daniel.poro.database.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditEntryDialogViewModel @Inject constructor(): ViewModel() {
    var session = Session()
}