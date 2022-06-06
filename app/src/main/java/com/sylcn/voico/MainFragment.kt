package com.sylcn.voico

import android.Manifest
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sylcn.voico.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var isRecordingStarted = false
    private var recorder: MediaRecorder? = null
    private var fileName: String = ""

    private var roundDrawable: Drawable? = null
    private var squareDrawable: Drawable? = null

    companion object {
        const val audioPermission = 122
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**This can be changed to more generic file format.
        For example it could be a name with date to give and
        We can record every audio separately.*/
        fileName = "${requireActivity().externalCacheDir?.absolutePath}/voicorecord.3gp"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        roundDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_round_button, null)!!
        squareDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_square_button, null)!!

        binding.recordButton.setOnClickListener {
            mainViewModel.onRecordClicked()
        }

        mainViewModel.isRecordingStarted.observe(viewLifecycleOwner) {
            when (it) {
                true -> startRecordingCommand()
                false -> stopRecordingCommand()
            }
        }

        mainViewModel.encodedAudioLiveData.observe(viewLifecycleOwner) {
            //Here we can access base64 string.
            print(it)
        }

        return binding.root
    }

    //Permisson check and record audio
    @AfterPermissionGranted(audioPermission)
    fun startRecordingCommand() {
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.RECORD_AUDIO)) {
            recorder = MediaRecorder()
            binding.recordText.text = getString(R.string.recording_started)
            binding.recordButton.setImageDrawable(squareDrawable)
            isRecordingStarted = true
            mainViewModel.startRecording(recorder!!, fileName)

        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.rationale_audio),
                audioPermission, Manifest.permission.READ_SMS
            )
        }
    }

    private fun stopRecordingCommand() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        binding.recordText.text = ""
        isRecordingStarted = false
        binding.recordButton.setImageDrawable(roundDrawable)

        mainViewModel.encodeAudio(fileName)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
}