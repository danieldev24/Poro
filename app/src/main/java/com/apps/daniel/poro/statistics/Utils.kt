/*
 * Copyright 2016-2021 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.daniel.poro.statistics

import android.content.Context
import com.apps.daniel.poro.R
import com.apps.daniel.poro.database.Label
import com.apps.daniel.poro.util.ThemeHelper

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