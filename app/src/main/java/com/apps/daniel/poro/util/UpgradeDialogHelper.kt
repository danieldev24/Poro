package com.apps.daniel.poro.util

import androidx.fragment.app.FragmentManager
import com.apps.daniel.poro.ui.upgrade_dialog.UpgradeDialog.Companion.showNewInstance

class UpgradeDialogHelper {
    companion object {
        @JvmStatic
        fun launchUpgradeDialog(fragmentManager: FragmentManager) {
            showNewInstance(fragmentManager)
        }
    }
}
