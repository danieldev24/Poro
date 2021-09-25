
package com.apps.daniel.poro.presentation.statistics.main

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(
    context: Context?,
    recyclerView: RecyclerView,
    private val listener: OnItemClickListener?
) : RecyclerView.OnItemTouchListener {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(view: View, position: Int)
    }

    private val gestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            val childView = recyclerView.findChildViewUnder(e.x, e.y)
            if (childView != null && listener != null) {
                listener.onItemLongClick(
                    childView,
                    recyclerView.getChildAdapterPosition(childView)
                )
            }
        }
    })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, view.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

}