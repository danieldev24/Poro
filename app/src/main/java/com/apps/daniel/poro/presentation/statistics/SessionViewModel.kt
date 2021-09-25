
package com.apps.daniel.poro.presentation.statistics

import com.apps.daniel.poro.domain.dao.SessionDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.daniel.poro.domain.AppDatabase
import com.apps.daniel.poro.domain.models.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    database: AppDatabase
) : ViewModel() {

    private val dao: SessionDao = database.sessionModel()

    val allSessions: LiveData<List<Session>>
        get() = dao.allSessions

    val allSessionsUnlabeled: LiveData<List<Session>>
        get() = dao.allSessionsUnlabeled

    val allSessionsUnarchived: LiveData<List<Session>>
        get() = dao.allSessionsUnarchived

    val allSessionsUnarchivedToday: LiveData<List<Session>>
        get() = dao.getAllSessionsUnarchivedToday()

    fun getSession(id: Long): LiveData<Session> {
        return dao.getSession(id)
    }

    fun addSession(session: Session) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.addSession(session)
        }
    }

    fun editSession(session: Session) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editSession(
                session.id, session.timestamp, session.duration, session.label
            )
        }
    }

    fun editLabel(id: Long?, label: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabel(
                id!!, label
            )
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSession(id)
        }
    }

    fun deleteSessionsFinishedToday() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSessionsAfter(
                LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
            )
        }
    }

    fun getSessions(label: String): LiveData<List<Session>> {
        return dao.getSessions(label)
    }
}