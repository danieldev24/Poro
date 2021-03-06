package com.apps.daniel.poro.bl

import android.content.ContextWrapper
import javax.inject.Inject
import com.apps.daniel.poro.presentation.settings.PreferenceHelper
import android.content.IntentFilter
import com.apps.daniel.poro.util.Constants.ACTION
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import com.apps.daniel.poro.util.Constants
import org.greenrobot.eventbus.EventBus
import com.apps.daniel.poro.util.Constants.UpdateTimerProgressEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import kotlin.math.min

class CurrentSessionManager @Inject constructor(
    @ApplicationContext val context: Context,
    val preferenceHelper: PreferenceHelper
) :
    ContextWrapper(context) {

    var currentSession = CurrentSession(
        TimeUnit.MINUTES.toMillis(preferenceHelper.getSessionDuration(SessionType.WORK)),
        preferenceHelper.currentSessionLabel.title
    )

    private lateinit var timer: AppCountDownTimer
    private var remaining: Long = 0 // [ms]

    private val alarmReceiver: AlarmReceiver =
        AlarmReceiver(object : AlarmReceiver.OnAlarmReceivedListener {
            override fun onAlarmReceived() {
                currentSession.setTimerState(TimerState.INACTIVE)
            }
        })

    private var sessionDuration: Long = 0

    fun startTimer(sessionType: SessionType) {
        Log.v(TAG, "startTimer: $sessionType")
        sessionDuration =
            TimeUnit.MINUTES.toMillis(preferenceHelper.getSessionDuration(sessionType))
        currentSession.setTimerState(TimerState.ACTIVE)
        currentSession.setSessionType(sessionType)
        currentSession.setDuration(sessionDuration)
        scheduleAlarm(
            sessionType, sessionDuration, preferenceHelper.oneMinuteBeforeNotificationEnabled()
                    && sessionDuration > TimeUnit.MINUTES.toMillis(1)
        )
        timer = AppCountDownTimer(sessionDuration)
        timer.start()
    }

    fun toggleTimer() {
        when (currentSession.timerState.value) {
            TimerState.PAUSED -> {
                Log.v(TAG, "toggleTimer PAUSED")
                scheduleAlarm(
                    currentSession.sessionType.value,
                    remaining,
                    preferenceHelper.oneMinuteBeforeNotificationEnabled()
                            && remaining > TimeUnit.MINUTES.toMillis(1)
                )
                timer.start()
                currentSession.setTimerState(TimerState.ACTIVE)
            }
            TimerState.ACTIVE -> {
                Log.v(TAG, "toggleTimer UNPAUSED")
                cancelAlarm()
                timer.cancel()
                timer = AppCountDownTimer(remaining)
                currentSession.setTimerState(TimerState.PAUSED)
            }
            else -> Log.wtf(TAG, "The timer is in an invalid state.")
        }
    }

    fun stopTimer() {
        cancelAlarm()
        timer.cancel()
        currentSession.setTimerState(TimerState.INACTIVE)
        currentSession.setSessionType(SessionType.INVALID)
    }

    /**
     * This is used to get the minutes that should be stored to the statistics
     * To be called when the session is finished without user interaction
     * @return the minutes elapsed
     */
    val elapsedMinutesAtFinished: Int
        get() {
            val sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration)
                .toInt()
            val extraMinutes = preferenceHelper.add60SecondsCounter
            return sessionMinutes + extraMinutes
        }

    /**
     * This is used to get the minutes that should be stored to the statistics
     * To be called when the user manually stops an ongoing session (or skips)
     * @return the minutes elapsed
     */
    val elapsedMinutesAtStop: Int
        get() {
            val sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration)
                .toInt()
            val extraMinutes = preferenceHelper.add60SecondsCounter
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining + 30000)
                .toInt()
            return sessionMinutes - remainingMinutes + extraMinutes
        }

    private fun scheduleAlarm(
        sessionType: SessionType?,
        duration: Long,
        remindOneMinuteLeft: Boolean
    ) {
        this.registerReceiver(alarmReceiver, IntentFilter(ACTION.FINISHED))
        val triggerAtMillis = duration + SystemClock.elapsedRealtime()
        Log.v(TAG, "scheduleAlarm " + sessionType.toString())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis, getAlarmPendingIntent(sessionType)
        )
        if (remindOneMinuteLeft && sessionType == SessionType.WORK) {
            Log.v(TAG, "scheduled one minute left")
            val triggerAtMillisOneMinuteLeft =
                duration - TimeUnit.MINUTES.toMillis(1) + SystemClock.elapsedRealtime()
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillisOneMinuteLeft, oneMinuteLeftAlarmPendingIntent
            )
        }
    }

    private fun cancelAlarm() {
        val intent = getAlarmPendingIntent(currentSession.sessionType.value)
        alarmManager.cancel(intent)
        val intentOneMinuteLeft = oneMinuteLeftAlarmPendingIntent
        alarmManager.cancel(intentOneMinuteLeft)
        unregisterAlarmReceiver()
    }

    private fun unregisterAlarmReceiver() {
        Log.v(TAG, "unregisterAlarmReceiver")
        try {
            unregisterReceiver(alarmReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "AlarmReceiver is already unregistered.")
        }
    }

    private val alarmManager: AlarmManager
        get() = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager

    private fun getAlarmPendingIntent(sessionType: SessionType?): PendingIntent {
        val intent = Intent(ACTION.FINISHED)
        intent.putExtra(Constants.SESSION_TYPE, sessionType.toString())
        return PendingIntent.getBroadcast(
            applicationContext, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val oneMinuteLeftAlarmPendingIntent: PendingIntent
        get() {
            val intent = Intent(ACTION.FINISHED)
            intent.putExtra(Constants.ONE_MINUTE_LEFT, true)
            return PendingIntent.getBroadcast(
                applicationContext, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    fun add60Seconds() {
        Log.v(TAG, "add60Seconds")
        val extra: Long = 60000 //TimeUnit.SECONDS.toMillis(60);
        cancelAlarm()
        timer.cancel()
        remaining = min(remaining + extra, TimeUnit.MINUTES.toMillis(240))
        timer = AppCountDownTimer(remaining)
        if (currentSession.timerState.value != TimerState.PAUSED) {
            scheduleAlarm(
                currentSession.sessionType.value,
                remaining,
                preferenceHelper.oneMinuteBeforeNotificationEnabled()
                        && remaining > TimeUnit.MINUTES.toMillis(1)
            )
            timer.start()
            currentSession.setTimerState(TimerState.ACTIVE)
        } else {
            currentSession.setDuration(remaining)
        }
    }

    private inner class AppCountDownTimer
        (millisInFuture: Long) : CountDownTimer(millisInFuture, 1000) {

        private val tAG = AppCountDownTimer::class.java.simpleName

        override fun onTick(millisUntilFinished: Long) {
            Log.v(tAG, "is Ticking: $millisUntilFinished millis remaining.")
            currentSession.setDuration(millisUntilFinished)
            remaining = millisUntilFinished
            EventBus.getDefault().post(UpdateTimerProgressEvent())
        }

        override fun onFinish() {
            Log.v(tAG, "is finished.")
            remaining = 0
        }
    }

    companion object {
        private val TAG = CurrentSessionManager::class.java.simpleName
    }

}