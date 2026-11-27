package com.example.swarstudio.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.isActive

class MicrophonePitchEngine(
    private val onPitch: (PitchReading?) -> Unit,
    private val onError: (String) -> Unit
) {
    @Volatile private var running = false
    private var recorder: AudioRecord? = null
    private var agc: AutomaticGainControl? = null
    private var noiseSuppressor: NoiseSuppressor? = null

    suspend fun start() = withContext(Dispatchers.IO) {
        if (running) return@withContext
        val rate = 44100
        val frameSize = 4096
        val minBytes = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBytes <= 0) { onError("এই ডিভাইসে অডিও ইনপুট শুরু করা যায়নি।"); return@withContext }
        val audio = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_IN_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
            .setBufferSizeInBytes(maxOf(minBytes * 2, frameSize * 2))
            .build()
        if (audio.state != AudioRecord.STATE_INITIALIZED) { audio.release(); onError("Microphone unavailable"); return@withContext }
        recorder = audio
        if (AutomaticGainControl.isAvailable()) agc = AutomaticGainControl.create(audio.audioSessionId)
        if (NoiseSuppressor.isAvailable()) noiseSuppressor = NoiseSuppressor.create(audio.audioSessionId)
        val detector = YinPitchDetector(rate)
        val buffer = ShortArray(frameSize)
        running = true
        try {
            audio.startRecording()
            while (running && coroutineContext.isActive) {
                val count = audio.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                if (count > 0) {
                    val frame = if (count == buffer.size) buffer else buffer.copyOf(count)
                    val hz = detector.detect(frame)
                    onPitch(hz?.let(::frequencyToReading))
                } else if (count < 0) onError("Audio read error: $count")
            }
        } catch (e: SecurityException) { onError("Microphone permission required") }
        catch (e: Exception) { onError(e.message ?: "Audio engine error") }
        finally { release() }
    }

    fun stop() { running = false; try { recorder?.stop() } catch (_: Exception) {} }
    private fun release() {
        try { agc?.release() } catch (_: Exception) {}; agc = null
        try { noiseSuppressor?.release() } catch (_: Exception) {}; noiseSuppressor = null
        try { recorder?.release() } catch (_: Exception) {}; recorder = null
        running = false
    }
}
