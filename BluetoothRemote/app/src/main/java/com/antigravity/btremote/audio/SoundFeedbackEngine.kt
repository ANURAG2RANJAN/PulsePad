package com.antigravity.btremote.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.antigravity.btremote.model.SoundProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance, zero-latency acoustic simulation engine for physical mechanical switches,
 * microswitches, and cyberpunk synth sounds.
 *
 * Uses SoundPool backed by procedurally generated 16-bit 44.1kHz PCM waveforms with dynamic
 * acoustic pitch jitter (±4%) to faithfully reproduce real physical mechanical keyboard typing.
 */
class SoundFeedbackEngine(private val context: Context) {
    private val TAG = "SoundFeedbackEngine"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Sound IDs mapped to profiles
    private var blueSwitchId: Int = 0
    private var brownSwitchId: Int = 0
    private var redSwitchId: Int = 0
    private var cyberSynthId: Int = 0
    private var mouseClickId: Int = 0
    private var spacebarThockId: Int = 0

    @Volatile
    var isReady: Boolean = false
        private set

    init {
        scope.launch {
            try {
                initSynthesizedSamples()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize synthesized audio samples", e)
            }
        }
    }

    private fun initSynthesizedSamples() {
        val cacheDir = File(context.cacheDir, "sounds")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val sampleRate = 44100

        // 1. Blue Switch: Sharp click-leaf snap + housing thock
        val bluePcm = generateBlueSwitch(sampleRate)
        val blueFile = File(cacheDir, "sw_blue.wav")
        writeWav(blueFile, bluePcm, sampleRate)
        blueSwitchId = soundPool.load(blueFile.absolutePath, 1)

        // 2. Brown Switch: Warm tactile bump
        val brownPcm = generateBrownSwitch(sampleRate)
        val brownFile = File(cacheDir, "sw_brown.wav")
        writeWav(brownFile, brownPcm, sampleRate)
        brownSwitchId = soundPool.load(brownFile.absolutePath, 1)

        // 3. Red Switch: Deep linear bottom-out
        val redPcm = generateRedSwitch(sampleRate)
        val redFile = File(cacheDir, "sw_red.wav")
        writeWav(redFile, redPcm, sampleRate)
        redSwitchId = soundPool.load(redFile.absolutePath, 1)

        // 4. Cyber Synth: High-tech frequency chirp
        val cyberPcm = generateCyberSynth(sampleRate)
        val cyberFile = File(cacheDir, "sw_cyber.wav")
        writeWav(cyberFile, cyberPcm, sampleRate)
        cyberSynthId = soundPool.load(cyberFile.absolutePath, 1)

        // 5. Mouse Microswitch Click
        val mousePcm = generateMouseMicroswitch(sampleRate)
        val mouseFile = File(cacheDir, "sw_mouse.wav")
        writeWav(mouseFile, mousePcm, sampleRate)
        mouseClickId = soundPool.load(mouseFile.absolutePath, 1)

        // 6. Spacebar Deep Cavity Thock
        val spacePcm = generateSpacebarThock(sampleRate)
        val spaceFile = File(cacheDir, "sw_space.wav")
        writeWav(spaceFile, spacePcm, sampleRate)
        spacebarThockId = soundPool.load(spaceFile.absolutePath, 1)

        isReady = true
        Log.d(TAG, "All acoustic synthesis sound profiles compiled and loaded into SoundPool")
    }

    /**
     * Plays a keypress acoustic sound with natural human pitch variations.
     */
    fun playKeyPress(
        profile: SoundProfile,
        volume: Float = 0.8f,
        isSpaceOrEnter: Boolean = false
    ) {
        if (!isReady || profile == SoundProfile.OFF || volume <= 0.01f) return

        val soundId = if (isSpaceOrEnter && profile != SoundProfile.CYBER_SYNTH) {
            spacebarThockId
        } else {
            when (profile) {
                SoundProfile.BLUE_SWITCH -> blueSwitchId
                SoundProfile.BROWN_SWITCH -> brownSwitchId
                SoundProfile.RED_SWITCH -> redSwitchId
                SoundProfile.CYBER_SYNTH -> cyberSynthId
                SoundProfile.OFF -> 0
            }
        }

        if (soundId == 0) return

        // Microscopic pitch jitter (±4%) simulates subtle acoustic variation of different keycap positions
        val pitch = 1.0f + (Random.nextFloat() - 0.5f) * 0.08f
        val clampedVol = volume.coerceIn(0.05f, 1.0f)

        soundPool.play(soundId, clampedVol, clampedVol, 1, 0, pitch)
    }

    /**
     * Plays a crisp microswitch mouse click sound.
     */
    fun playMouseClick(volume: Float = 0.8f) {
        if (!isReady || mouseClickId == 0 || volume <= 0.01f) return
        val pitch = 1.0f + (Random.nextFloat() - 0.5f) * 0.06f
        val clampedVol = volume.coerceIn(0.05f, 1.0f)
        soundPool.play(mouseClickId, clampedVol, clampedVol, 1, 0, pitch)
    }

    fun release() {
        soundPool.release()
    }

    // -------------------------------------------------------------
    // Procedural Acoustic Waveform Synthesis Models
    // -------------------------------------------------------------

    private fun generateBlueSwitch(sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * 0.042).toInt() // 42 ms
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Component A: Plastic click-leaf snapping impulse (~3800 Hz)
            val decaySnap = exp(-t / 0.0035)
            val snap = sin(2 * PI * 3800 * t) * decaySnap + (Random.nextDouble(-1.0, 1.0) * 0.28 * decaySnap)

            // Component B: Keycap bottom-out thock (~430 Hz fundamental + 860 Hz harmonic)
            val decayThock = exp(-t / 0.016)
            val thock = (sin(2 * PI * 430 * t) * 0.75 + sin(2 * PI * 860 * t) * 0.35) * decayThock

            val sample = (snap * 0.65 + thock * 0.55).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 30000).toInt().toShort()
        }
        return pcm
    }

    private fun generateBrownSwitch(sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * 0.035).toInt() // 35 ms
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t / 0.013)
            val tactileBump = sin(2 * PI * 540 * t) * 0.7 + sin(2 * PI * 310 * t) * 0.5
            val overtone = sin(2 * PI * 1350 * t) * 0.25 * exp(-t / 0.004)
            val sample = ((tactileBump + overtone) * decay).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 29000).toInt().toShort()
        }
        return pcm
    }

    private fun generateRedSwitch(sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * 0.032).toInt() // 32 ms
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t / 0.017)
            val thock = sin(2 * PI * 280 * t) * 0.85 + sin(2 * PI * 610 * t) * 0.35
            val tap = (Random.nextDouble(-1.0, 1.0) * 0.15) * exp(-t / 0.003)
            val sample = ((thock + tap) * decay).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 28000).toInt().toShort()
        }
        return pcm
    }

    private fun generateCyberSynth(sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * 0.036).toInt() // 36 ms
        val duration = 0.036
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t / 0.018)
            // Chirp downwards: 1900 Hz to 650 Hz
            val f = 1900.0 - (1250.0 * (t / duration))
            val phase = 2 * PI * (1900.0 * t - (1250.0 / (2 * duration)) * t * t)
            val chirp = sin(phase) * 0.75 + sin(phase * 2.0) * 0.25
            val sample = (chirp * decay).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 29000).toInt().toShort()
        }
        return pcm
    }

    private fun generateMouseMicroswitch(sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * 0.022).toInt() // 22 ms
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val snapDecay = exp(-t / 0.0022)
            val bodyDecay = exp(-t / 0.009)
            val snap = sin(2 * PI * 4900 * t) * snapDecay + (Random.nextDouble(-1.0, 1.0) * 0.18 * snapDecay)
            val body = sin(2 * PI * 1350 * t) * bodyDecay * 0.45
            val sample = (snap * 0.7 + body * 0.4).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 30000).toInt().toShort()
        }
        return pcm
    }

    private fun generateSpacebarThock(sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * 0.048).toInt() // 48 ms
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t / 0.024)
            val cavity = sin(2 * PI * 210 * t) * 0.9 + sin(2 * PI * 420 * t) * 0.35 + sin(2 * PI * 110 * t) * 0.4
            val sample = (cavity * decay).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 31000).toInt().toShort()
        }
        return pcm
    }

    private fun writeWav(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 1 * 2 // 1 channel, 16 bits

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat 1 = PCM
        header[21] = 0
        header[22] = 1 // 1 Channel
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // BlockAlign
        header[33] = 0
        header[34] = 16 // BitsPerSample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(file).use { fos ->
            fos.write(header)
            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                byteBuffer.putShort(s)
            }
            fos.write(byteBuffer.array())
        }
    }
}
