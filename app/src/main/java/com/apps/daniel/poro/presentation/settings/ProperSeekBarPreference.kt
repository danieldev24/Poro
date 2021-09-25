package com.apps.daniel.poro.presentation.settings

import android.content.Context
import kotlin.jvm.JvmOverloads
import com.apps.daniel.poro.R
import android.widget.SeekBar
import android.widget.TextView
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.preference.PreferenceViewHolder
import android.content.res.TypedArray
import android.os.Parcelable
import android.os.Parcel
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.preference.DialogPreference
import kotlin.math.abs
import kotlin.math.min

class ProperSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarPreferenceStyle,
    defStyleRes: Int = 0
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {
    var mSeekBarValue = 0
    var mMin: Int
    private var mMax = 0
    private var mSeekBarIncrement = 0
    var mTrackingTouch = false
    var mSeekBar: SeekBar? = null
    private var mSeekBarValueTextView: TextView? = null
    var isAdjustable: Boolean

    // Whether to show the SeekBar value TextView next to the bar
    private var mShowSeekBarValue: Boolean

    var updatesContinuously: Boolean

    private var seekBarIncrement: Int
        get() = mSeekBarIncrement
        set(seekBarIncrement) {
            if (seekBarIncrement != mSeekBarIncrement) {
                mSeekBarIncrement = min(mMax - mMin, abs(seekBarIncrement))
                notifyChanged()
            }
        }

    private var max: Int
        get() = mMax
        set(max) {
            var maxTmp = max
            if (maxTmp < mMin) {
                maxTmp = mMin
            }
            if (maxTmp != mMax) {
                mMax = maxTmp
                notifyChanged()
            }
        }

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes
        )

        mMin = a.getInt(R.styleable.SeekBarPreference_min, 0)
        max = a.getInt(R.styleable.SeekBarPreference_android_max, 100)
        seekBarIncrement = a.getInt(R.styleable.SeekBarPreference_seekBarIncrement, 0)
        isAdjustable = a.getBoolean(R.styleable.SeekBarPreference_adjustable, true)
        mShowSeekBarValue = a.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, false)
        updatesContinuously = a.getBoolean(
            R.styleable.SeekBarPreference_updatesContinuously,
            false
        )
        a.recycle()
    }

    /**
     * Listener reacting to the [SeekBar] changing value by the user
     */
    private val mSeekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser && (updatesContinuously || !mTrackingTouch)) {
                syncValueInternal(seekBar)
            } else {
                // We always want to update the text while the seekbar is being dragged
                updateLabelValue(progress + mMin)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            mTrackingTouch = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            mTrackingTouch = false
            if (seekBar.progress + mMin != mSeekBarValue) {
                syncValueInternal(seekBar)
            }
        }
    }

    /**
     * Listener reacting to the user pressing DPAD left/right keys if `adjustable` attribute is set to true; it transfers the key presses to the [SeekBar]
     * to be handled accordingly.
     */
    private val mSeekBarKeyListener: View.OnKeyListener = object : View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action != KeyEvent.ACTION_DOWN) {
                return false
            }
            if (!isAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                        || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
            ) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false
            }

            // We don't want to propagate the click keys down to the SeekBar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false
            }
            if (mSeekBar == null) {
                Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.")
                return false
            }
            return mSeekBar!!.onKeyDown(keyCode, event)
        }
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        view.itemView.setOnKeyListener(mSeekBarKeyListener)
        mSeekBar = view.findViewById(R.id.seekbar) as SeekBar
        mSeekBarValueTextView = view.findViewById(R.id.seekbar_value) as TextView
        if (mShowSeekBarValue) {
            mSeekBarValueTextView!!.visibility = View.VISIBLE
        } else {
            mSeekBarValueTextView!!.visibility = View.GONE
            mSeekBarValueTextView = null
        }
        if (mSeekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.")
            return
        }
        mSeekBar!!.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mSeekBar!!.max = mMax - mMin
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (mSeekBarIncrement != 0) {
            mSeekBar!!.keyProgressIncrement = mSeekBarIncrement
        } else {
            mSeekBarIncrement = mSeekBar!!.keyProgressIncrement
        }
        mSeekBar!!.progress = mSeekBarValue - mMin
        updateLabelValue(mSeekBarValue)
        mSeekBar!!.isEnabled = isEnabled
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        var defaultValueTmp = defaultValue
        if (defaultValueTmp == null) {
            defaultValueTmp = 0
        }
        value = getPersistedInt((defaultValueTmp as Int?)!!)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    var showSeekBarValue: Boolean
        get() = mShowSeekBarValue
        set(showSeekBarValue) {
            mShowSeekBarValue = showSeekBarValue
            notifyChanged()
        }

    private fun setValueInternal(seekBarValue: Int, notifyChanged: Boolean) {
        var seekBarValueTmp = seekBarValue
        if (seekBarValueTmp < mMin) {
            seekBarValueTmp = mMin
        }
        if (seekBarValueTmp > mMax) {
            seekBarValueTmp = mMax
        }
        if (seekBarValueTmp != mSeekBarValue) {
            mSeekBarValue = seekBarValueTmp
            updateLabelValue(mSeekBarValue)
            persistInt(seekBarValueTmp)
            if (notifyChanged) {
                notifyChanged()
            }
        }
    }

    var value: Int
        get() = mSeekBarValue
        set(seekBarValue) {
            setValueInternal(seekBarValue, true)
        }

    /**
     * Persist the [SeekBar]'s SeekBar value if callChangeListener returns true, otherwise
     * set the [SeekBar]'s value to the stored value.
     */
    fun syncValueInternal(seekBar: SeekBar) {
        val seekBarValue = mMin + seekBar.progress
        if (seekBarValue != mSeekBarValue) {
            if (callChangeListener(seekBarValue)) {
                setValueInternal(seekBarValue, false)
            } else {
                seekBar.progress = mSeekBarValue - mMin
                updateLabelValue(mSeekBarValue)
            }
        }
    }

    /**
     * Attempts to update the TextView label that displays the current value.
     *
     * @param value the value to display next to the [SeekBar]
     */
    fun updateLabelValue(value: Int) {
        if (mSeekBarValueTextView != null) {
            mSeekBarValueTextView!!.text = value.toString()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        // Save the instance state
        val myState = SavedState(superState)
        myState.mSeekBarValue = mSeekBarValue
        myState.mMin = mMin
        myState.mMax = mMax
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        // Restore the instance state
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        mSeekBarValue = myState.mSeekBarValue
        mMin = myState.mMin
        mMax = myState.mMax
        notifyChanged()
    }

    private class SavedState(superState: Parcelable?) : BaseSavedState(superState) {
        var mSeekBarValue = 0
        var mMin = 0
        var mMax = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)

            // Save the click counter
            dest.writeInt(mSeekBarValue)
            dest.writeInt(mMin)
            dest.writeInt(mMax)
        }
    }

    companion object {
        private const val TAG = "SeekBarPreference"
    }
}