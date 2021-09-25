
package com.apps.daniel.poro.presentation.all_sessions

import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import com.apps.daniel.poro.R
import android.content.res.ColorStateList
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.apps.daniel.poro.domain.models.Label
import com.apps.daniel.poro.domain.models.Session
import com.apps.daniel.poro.helpers.ThemeHelper
import java.lang.ref.WeakReference
import java.util.ArrayList

class AllSessionsAdapter internal constructor(labels: List<Label>) :
    RecyclerView.Adapter<AllSessionsViewHolder>() {

    private lateinit var mContext: WeakReference<Context>
    var mEntries: MutableList<Session> = ArrayList()
    var mSelectedEntries: MutableList<Long> = ArrayList()
    val labels: List<Label>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSessionsViewHolder {
        mContext = WeakReference(parent.context)
        val inflater = LayoutInflater.from(mContext.get())
        val statusContainer = inflater.inflate(R.layout.statistics_all_sessions_row, parent, false)
        return AllSessionsViewHolder(statusContainer)
    }

    override fun onBindViewHolder(holder: AllSessionsViewHolder, position: Int) {
        val session = mEntries[position]
        holder.bind(session, ColorStateList.valueOf(getColor(session.label)))
        holder.rowOverlay.visibility =
            if (mSelectedEntries.contains(mEntries[position].id)) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return mEntries.size
    }

    override fun getItemId(position: Int): Long {
        val session = mEntries[position]
        return session.id
    }

    private fun getColor(label: String?) = ThemeHelper.getColor(
        mContext.get()!!, labels.findLast {
            it.title == label } ?.colorId ?: ThemeHelper.COLOR_INDEX_UNLABELED)

    fun setData(newSessions: List<Session>) {
        val postDiffCallback = PostDiffCallback(mEntries, newSessions)
        val diffResult = DiffUtil.calculateDiff(postDiffCallback)
        mEntries.clear()
        mEntries.addAll(newSessions)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setSelectedItems(selectedItems: MutableList<Long>) {
        mSelectedEntries = selectedItems
        notifyDataSetChanged()
    }

    internal class PostDiffCallback(
        private val oldSessions: List<Session>,
        private val newSessions: List<Session>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldSessions.size
        }

        override fun getNewListSize(): Int {
            return newSessions.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldSessions[oldItemPosition].id == newSessions[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldSessions[oldItemPosition] == newSessions[newItemPosition]
        }
    }

    init {
        // this and the override of getItemId are to avoid clipping in the view
        setHasStableIds(true)
        this.labels = labels
    }
}