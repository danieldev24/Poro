package com.apps.daniel.poro.domain.models

import androidx.annotation.Nullable
import androidx.room.*
import com.apps.daniel.poro.util.millis
import java.time.LocalDateTime

@Entity(
    foreignKeys = [ForeignKey(
        entity = Label::class,
        parentColumns = ["title", "archived"],
        childColumns = ["label", "archived"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
class Session(
    @field:PrimaryKey(autoGenerate = true)var id: Long,
    var timestamp: Long,
    var duration: Int,
    @Nullable
    var label: String?
) {
    @ColumnInfo(name = "archived", defaultValue = "0")
    var archived = false

    @Ignore
    constructor() : this(0, LocalDateTime.now().millis, 0, null)
}
