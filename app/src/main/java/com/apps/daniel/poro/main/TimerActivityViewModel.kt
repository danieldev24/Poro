
package com.apps.daniel.poro.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.apps.daniel.poro.bl.SessionType

class TimerActivityViewModel(application: Application) : AndroidViewModel(application) {
    var isActive = false
    var showFinishDialog = false
    var dialogPendingType: SessionType = SessionType.INVALID
    var enableFlashingNotification = false
}
