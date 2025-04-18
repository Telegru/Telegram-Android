package ru.dahl.messenger.settings.model

import androidx.annotation.StringRes
import org.telegram.messenger.R

enum class CameraType {

    FRONT, BACK, ALWAYS_ASK;

    @get:StringRes
    val titleLong: Int
        get() = when (this) {
            ALWAYS_ASK -> R.string.CameraAsk
            FRONT -> R.string.CameraFront
            BACK -> R.string.CameraRear
        }

    @get:StringRes
    val titleShort: Int
        get() = when (this) {
            ALWAYS_ASK -> R.string.Ask
            FRONT -> R.string.Front
            BACK -> R.string.Rear
        }
}