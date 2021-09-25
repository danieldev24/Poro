package com.apps.daniel.poro.presentation.labels

import android.content.Context
import androidx.lifecycle.*
import com.apps.daniel.poro.domain.AppDatabase
import com.apps.daniel.poro.domain.dao.LabelDao
import com.apps.daniel.poro.domain.models.Label
import com.apps.daniel.poro.presentation.statistics.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: AppDatabase
) : ViewModel() {

    private val dao: LabelDao = database.labelDao()

    /**
     * The current selected label in the Statistics view
     * "extended" because it might be "total" or "unlabeled"
     */
    val crtExtendedLabel = MutableLiveData<Label>()

    /**
     * Returns only the labels which are not archived
     */
    val labels: LiveData<List<Label>>
        get() = dao.labels

    /**
     * Returns all labels, including the archived ones
     */
    val allLabels: LiveData<List<Label>>
        get() = dao.allLabels

    fun getColorOfLabel(label: String): LiveData<Int> {
        return dao.getColor(label)
    }

    fun addLabel(label: Label) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.addLabel(label)
        }
    }

    fun editLabelName(label: String, newLabel: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabelName(label, newLabel)
        }
    }

    fun editLabelColor(label: String, color: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabelColor(label, color)
        }
    }

    fun editLabelOrder(label: String, newOrder: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabelOrder(label, newOrder)
        }
    }

    fun toggleLabelArchive(label: String, archived: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.toggleLabelArchiveState(label, archived)
        }
    }

    fun deleteLabel(label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteLabel(label)
        }
    }

    init {
        crtExtendedLabel.value =
            Utils.getInstanceTotalLabel(context)
    }
}