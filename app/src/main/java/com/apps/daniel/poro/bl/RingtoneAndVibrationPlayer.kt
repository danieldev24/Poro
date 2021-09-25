package com.apps.daniel.poro.bl

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Vibrator
import com.apps.daniel.poro.presentation.settings.PreferenceHelper
import com.apps.daniel.poro.presentation.settings.toRingtone
import com.apps.daniel.poro.util.VibrationPatterns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class RingtoneAndVibrationPlayer @Inject constructor(
    @ApplicationContext val context: Context,
    val preferenceHelper: PreferenceHelper
) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val audioManager: AudioManager =
        context.getSystemService(AUDIO_SERVICE) as AudioManager

    fun play(sessionType: SessionType, insistent: Boolean) {
        try {
            vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (preferenceHelper.isRingtoneEnabled()) {
                val ringtoneRaw =
                    if (sessionType == SessionType.WORK) preferenceHelper.getNotificationSoundWorkFinished()
                    else preferenceHelper.getNotificationSoundBreakFinished()

                val uri = Uri.parse(ringtoneRaw?.let { toRingtone(it).uri })

                mediaPlayer = MediaPlayer()
                mediaPlayer?.let {mp->
                    mp.setDataSource(context, uri)
                    audioManager.mode = AudioManager.MODE_NORMAL
                    val attributes = Builder()
                        .setUsage(if (preferenceHelper.isPriorityAlarm()) USAGE_ALARM else USAGE_NOTIFICATION)
                        .build()
                    mp.setAudioAttributes(attributes)
                    mp.isLooping = insistent
                    mp.prepareAsync()
                    mp.setOnPreparedListener {
                        // TODO: check duration of custom ringtones which may be much longer than notification sounds.
                        // If it's n seconds long and we're in continuous mode,
                        // schedule a stop after x seconds.
                        it.start()
                    }
                }
            }
            val vibrationType = preferenceHelper.getVibrationType()
            if (vibrationType > 0) {
                vibrator?.vibrate(
                    VibrationPatterns.LIST[vibrationType],
                    if (insistent) 2 else -1
                )
            }
        } catch (e: SecurityException) {
            stop()
        } catch (e: IOException) {
            stop()
        }
    }

    fun stop() {
        mediaPlayer?.let { mp->
            vibrator?.let { vib->
                mp.reset()
                mp.release()
                mediaPlayer = null
                vib.cancel()
            }
        }
    }

}