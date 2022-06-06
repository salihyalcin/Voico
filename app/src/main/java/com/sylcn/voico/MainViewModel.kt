package com.sylcn.voico

import android.media.MediaRecorder
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private var _isRecordingStarted: MutableLiveData<Boolean> = MutableLiveData(false)

    val isRecordingStarted: LiveData<Boolean>
        get() = _isRecordingStarted

    private val _base64AudioString: MutableLiveData<String> = MutableLiveData("")

    val encodedAudioLiveData: LiveData<String>
        get() = _base64AudioString

    fun onRecordClicked() {
        _isRecordingStarted.value = !_isRecordingStarted.value!!
    }

    fun startRecording(recorder: MediaRecorder, fileName: String) {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.setOutputFile(fileName)
        recorder.prepare()
        recorder.start()
    }


    fun encodeAudio(selectedPath: String) {
        val audioBytes: ByteArray
        try {
            val audioFile = File(selectedPath)
            val fileSize = audioFile.length()
            val byteArrayOutputStream = ByteArrayOutputStream()
            val fis = FileInputStream(File(selectedPath))
            val buf = ByteArray(1024)
            var n: Int
            while (-1 != fis.read(buf).also { n = it }) byteArrayOutputStream.write(buf, 0, n)
            audioBytes = byteArrayOutputStream.toByteArray()

            val audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT)
            _base64AudioString.postValue(audioBase64)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}