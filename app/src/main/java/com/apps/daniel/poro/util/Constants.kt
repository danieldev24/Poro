
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
    const val SESSION_TYPE = "poro.session.type"
    const val ONE_MINUTE_LEFT = "poro.one.minute.left"
    const val sku = "upgraded_version"


    interface ACTION {
        companion object {
            const val START = "poro.action.start"
            const val SKIP = "poro.action.skip"
            const val TOGGLE = "poro.action.toggle"
            const val STOP = "poro.action.stop"
            const val FINISHED = "poro.action.finished"
            const val ADD_SECONDS = "poro.action.addseconds"
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