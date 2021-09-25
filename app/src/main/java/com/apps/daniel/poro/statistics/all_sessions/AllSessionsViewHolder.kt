
package com.apps.daniel.poro.statistics.all_sessions

import android.content.res.ColorStateList
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.apps.daniel.poro.database.Session
import com.apps.daniel.poro.databinding.StatisticsAllSessionsRowBinding

class AllSessionsViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(
    itemView
) {
    private var binding: StatisticsAllSessionsRowBinding = DataBindingUtil.bind(itemView)!!

    val rowOverlay: View = binding.overlay
    fun bind(item: Session, color: ColorStateList) {
        binding.item = item
        binding.status.chipBackgroundColor = color
    }

}