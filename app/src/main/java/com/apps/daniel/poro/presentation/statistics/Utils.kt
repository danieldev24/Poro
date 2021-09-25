
package com.apps.daniel.poro.presentation.statistics

import android.content.Context
import com.apps.daniel.poro.R
import com.apps.daniel.poro.domain.models.Label
import com.apps.daniel.poro.helpers.ThemeHelper

object Utils {
    fun getInstanceTotalLabel(context: Context): Label {
        return Label(
            context.getString(R.string.label_all),
            ThemeHelper.COLOR_INDEX_ALL_LABELS
        )
    }

    fun getInstanceUnlabeledLabel(context: Context): Label {
        return Label(
            "unlabeled", ThemeHelper.getColor(
                context,
                ThemeHelper.COLOR_INDEX_UNLABELED
            )
        )
    }

    fun getInvalidLabel(context: Context): Label {
        return Label(
            "", ThemeHelper.getColor(
                context,
                ThemeHelper.COLOR_INDEX_UNLABELED
            )
        )
    }
}