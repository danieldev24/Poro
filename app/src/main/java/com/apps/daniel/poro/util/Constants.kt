
package com.apps.daniel.poro.util

object Constants {
    const val PROFILE_NAME_DEFAULT = "25/5"
    const val PROFILE_NAME_52_17 = "52/17"
    const val DEFAULT_WORK_DURATION_DEFAULT = 25
    const val DEFAULT_BREAK_DURATION_DEFAULT = 5
    const val DEFAULT_LONG_BREAK_DURATION = 15
    const val DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4
    const val DEFAULT_WORK_DURATION_5217 = 52
    const val DEFAULT_BREAK_DURATION_5217 = 17
    const val SESSION_TYPE = "goodtime.session.type"
    const val ONE_MINUTE_LEFT = "goodtime.one.minute.left"
    const val sku = "upgraded_version"


    interface ACTION {
        companion object {
            const val START = "goodtime.action.start"
            const val SKIP = "goodtime.action.skip"
            const val TOGGLE = "goodtime.action.toggle"
            const val STOP = "goodtime.action.stop"
            const val FINISHED = "goodtime.action.finished"
            const val ADD_SECONDS = "goodtime.action.addseconds"
        }
    }

    class FinishWorkEvent
    class FinishBreakEvent
    class FinishLongBreakEvent
    class UpdateTimerProgressEvent
    class ClearNotificationEvent
    class StartSessionEvent
    class OneMinuteLeft
}