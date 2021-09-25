package com.apps.daniel.poro.presentation.statistics.main

import android.graphics.Canvas
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils

class CustomXAxisRenderer(viewPortHandler: ViewPortHandler?, xAxis: XAxis?, trans: Transformer?) :
    XAxisRenderer(viewPortHandler, xAxis, trans) {
    override fun drawLabel(
        c: Canvas,
        formattedLabel: String,
        x: Float,
        y: Float,
        anchor: MPPointF,
        angleDegrees: Float
    ) {
        val lines = formattedLabel.split("\n".toRegex()).toTypedArray()
        for (i in lines.indices) {
            val vOffset = i * mAxisLabelPaint.textSize
            Utils.drawXAxisValue(c, lines[i], x, y + vOffset, mAxisLabelPaint, anchor, angleDegrees)
        }
    }
}