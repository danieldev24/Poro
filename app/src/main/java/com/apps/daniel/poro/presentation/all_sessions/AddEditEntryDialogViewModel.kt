
package com.apps.daniel.poro.presentation.all_sessions

import androidx.lifecycle.ViewModel
import com.apps.daniel.poro.domain.models.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditEntryDialogViewModel @Inject constructor(): ViewModel() {
    var session = Session()
}