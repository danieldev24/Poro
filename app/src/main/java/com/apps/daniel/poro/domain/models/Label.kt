package com.apps.daniel.poro.domain.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["title", "archived"])
class Label(@ColumnInfo(name = "title", defaultValue = "") @NonNull var title: String, var colorId: Int) {
    var order = 0
    var archived = false
}