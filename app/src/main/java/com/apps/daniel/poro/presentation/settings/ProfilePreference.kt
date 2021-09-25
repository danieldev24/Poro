package com.apps.daniel.poro.presentation.settings

import kotlin.jvm.JvmOverloads
import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class ProfilePreference @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int = 0
) : ListPreference(context, attrs, defStyleAttr, defStyleRes) {
    interface ProfileChangeListener {
        fun onProfileChange(newValue: CharSequence?)
    }

    private var mChangeListener: ProfileChangeListener? = null

    fun attachListener(changeListener: ProfileChangeListener?) {
        mChangeListener = changeListener
    }

    override fun setValue(value: String) {
        super.setValue(value)
        if (mChangeListener != null) {
            mChangeListener!!.onProfileChange(getValue())
        }
    }
}