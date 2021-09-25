
package com.apps.daniel.poro.util

import android.content.Context
import android.content.Intent
import com.apps.daniel.poro.bl.SessionType
import com.apps.daniel.poro.util.Constants.SESSION_TYPE

class IntentWithAction : Intent {
    constructor(context: Context, cls: Class<*>, action: String) : super(context, cls) {
        this.action = action
    }

    constructor(
        context: Context,
        cls: Class<*>,
        action: String,
        sessionType: SessionType
    ) : super(context, cls) {
        this.action = action
        this.putExtra(SESSION_TYPE, sessionType.toString())
    }
}