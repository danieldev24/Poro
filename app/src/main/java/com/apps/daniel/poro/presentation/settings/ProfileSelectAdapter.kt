package com.apps.daniel.poro.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apps.daniel.poro.R
import android.widget.CheckedTextView
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.*

class ProfileSelectAdapter(
    context: Context,
    profiles: Array<CharSequence>,
    selectedIndex: Int,
    callback: OnProfileSelectedListener
) : RecyclerView.Adapter<ProfileSelectAdapter.ViewHolder>() {
    interface OnProfileSelectedListener {
        fun onDelete(position: Int)
        fun onSelect(position: Int)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val mContext: WeakReference<Context> = WeakReference(context)
    private val mProfiles: MutableList<CharSequence>
    private var mClickedDialogEntryIndex: Int
    private val mCallback: OnProfileSelectedListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.dialog_select_profile_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val profile = mProfiles[position].toString()

        // don't delete the predefined profiles
        val showDeleteButton = profile != mContext.get()!!.getString(R.string.pref_profile_5217) &&
                profile != mContext.get()!!.getString(R.string.pref_profile_default)
        holder.text.text = profile
        if (mClickedDialogEntryIndex != -1) {
            holder.text.isChecked = mProfiles[mClickedDialogEntryIndex] == profile
        }
        holder.deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
        holder.deleteButton.setOnClickListener {
            mCallback.onDelete(position)
            mProfiles.removeAt(position)
            notifyItemRemoved(position)
            notifyItemChanged(position)
            if (position == mClickedDialogEntryIndex) {
                mClickedDialogEntryIndex = 0
                // 1 because of the predefined profiles (25/5 is 0, 52/17 is 1)
            } else if (position in 2 until mClickedDialogEntryIndex) {
                --mClickedDialogEntryIndex
            }
        }
        holder.text.setOnClickListener {
            mClickedDialogEntryIndex = position
            mCallback.onSelect(position)
        }
    }

    override fun getItemCount(): Int {
        return mProfiles.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: CheckedTextView = itemView.findViewById(R.id.text)
        val deleteButton: FrameLayout = itemView.findViewById(R.id.image_delete_container)

    }

    init {
        mProfiles = ArrayList(listOf(*profiles))
        mClickedDialogEntryIndex = selectedIndex
        mCallback = callback
    }
}