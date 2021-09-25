
package com.apps.daniel.poro.statistics.main

import com.apps.daniel.poro.util.StringUtils
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Custom formatter used to print the day of the week or the hour of the day.
 */
internal class ProductiveTimeXAxisFormatter(private val mType: SpinnerProductiveTimeType, private val is24HourFormat: Boolean) :
    ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        return if (mType == SpinnerProductiveTimeType.HOUR_OF_DAY && value < 24 && value >= 0) {
            StringUtils.toHourOfDay(value.toInt(), is24HourFormat)
        } else if (mType == SpinnerProductiveTimeType.DAY_OF_WEEK && value < 7 && value >= 0) {
            StringUtils.toDayOfWeek((value + 1).toInt())
        } else {
            ""
        }
    }
}