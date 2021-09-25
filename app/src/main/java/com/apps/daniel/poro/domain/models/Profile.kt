package com.apps.daniel.poro.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Profile {
    @Ignore
    constructor(
        name: String, durationWork: Int, durationBreak: Int
    ) {
        this.name = name
        this.durationWork = durationWork
        this.durationBreak = durationBreak
        enableLongBreak = false
        durationLongBreak = 15
        sessionsBeforeLongBreak = 4
    }

    constructor(
        name: String,
        durationWork: Int,
        durationBreak: Int,
        durationLongBreak: Int,
        sessionsBeforeLongBreak: Int
    ) {
        this.name = name
        this.durationWork = durationWork
        this.durationBreak = durationBreak
        enableLongBreak = true
        this.durationLongBreak = durationLongBreak
        this.sessionsBeforeLongBreak = sessionsBeforeLongBreak
    }

    @PrimaryKey
    var name: String
    var durationWork: Int
    var durationBreak: Int
    var enableLongBreak: Boolean
    var durationLongBreak: Int
    var sessionsBeforeLongBreak: Int
}