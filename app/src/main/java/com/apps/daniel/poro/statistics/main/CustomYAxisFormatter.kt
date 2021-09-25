
package com.apps.daniel.poro.statistics.main

import com.apps.daniel.poro.util.StringUtils
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class CustomYAxisFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        return StringUtils.formatLong(value.toLong())
    }
}