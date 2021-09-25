
package com.apps.daniel.poro.bl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.greenrobot.eventbus.EventBus
import com.apps.daniel.poro.util.Constants.OneMinuteLeft
import com.apps.daniel.poro.util.Constants
import com.apps.daniel.poro.util.Constants.FinishWorkEvent
import com.apps.daniel.poro.util.Constants.FinishBreakEvent
import com.apps.daniel.poro.util.Constants.FinishLongBreakEvent

class AlarmReceiver(val listener: OnAlarmReceivedListener) : BroadcastReceiver() {

    interface OnAlarmReceivedListener {
        fun onAlarmReceived()
    }

    companion object{
        private val TAG = AlarmReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        val oneMinuteLeft = intent.getBooleanExtra(Constants.ONE_MINUTE_LEFT, false)
        if (oneMinuteLeft) {
            Log.v(TAG, "onReceive oneMinuteLeft")
            EventBus.getDefault().post(OneMinuteLeft())
            return
        }
        val sessionType = SessionType.valueOf(intent.getStringExtra(Constants.SESSION_TYPE)!!)
        Log.v(TAG, "onReceive $sessionType")
        
        listener.onAlarmReceived()

        when (sessionType) {
            SessionType.WORK -> EventBus.getDefault().post(FinishWorkEvent())
            SessionType.BREAK -> EventBus.getDefault().post(FinishBreakEvent())
            SessionType.LONG_BREAK -> EventBus.getDefault().post(FinishLongBreakEvent())
            else -> {
            }
        }
    }
}