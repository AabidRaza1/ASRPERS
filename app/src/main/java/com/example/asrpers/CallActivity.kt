package com.example.asrpers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class CallActivity : AppCompatActivity() {
    private lateinit var contactNameTextView: TextView
    private lateinit var contactImageView: ImageView
    private lateinit var callTimerTextView: TextView
    private lateinit var buttonMute: ImageButton
    private lateinit var buttonSpeaker: ImageButton
    private lateinit var buttonEndCall: Button
    private val END_CALL_PERMISSION_REQUEST_CODE = 100

    private var isMuted: Boolean = false
    private var isSpeakerOn: Boolean = false

    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.call_activity)

        contactNameTextView = findViewById(R.id.contactNameTextView)
        contactImageView = findViewById(R.id.contactImageView)
        callTimerTextView = findViewById(R.id.callTimerTextView)
        buttonMute = findViewById(R.id.buttonMute)
        buttonSpeaker = findViewById(R.id.buttonSpeaker)
        buttonEndCall = findViewById(R.id.buttonEndCall)


        // Retrieve contact details from the intent
        val name = intent.getStringExtra("name")
        val image = if (intent.hasExtra("image")) {
            intent.getIntExtra("image", R.mipmap.ic_add)
        } else {
            R.mipmap.ic_add
        }

        phoneNumber = intent.getStringExtra("phone") ?: ""
        makeCall()
        // Update the UI with the contact details
        contactNameTextView.text = name
        contactImageView.setImageResource(image)
        // Update any other UI elements as needed

        makeCall()
        buttonEndCall.setOnClickListener {
            endCall()
        }

        // Mute/Unmute button click listener
        buttonMute.setOnClickListener {
            isMuted = !isMuted
            if (isMuted) {
                // Set the button icon for mute state
                buttonMute.setImageResource(R.mipmap.ic_mic_muted)
                // Perform mute operation
                mute()
            } else {
                // Set the button icon for unmute state
                buttonMute.setImageResource(R.mipmap.ic_mic_unmuted)
                // Perform unmute operation
                unmute()
            }
        }

        // Speaker on/off button click listener
        buttonSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            if (isSpeakerOn) {
                // Set the button icon for speaker on state
                buttonSpeaker.setImageResource(R.mipmap.ic_speaker_on)
                // Perform speaker on operation
                speakerOn()
            } else {
                // Set the button icon for speaker off state
                buttonSpeaker.setImageResource(R.mipmap.ic_speaker_off)
                // Perform speaker off operation
                speakerOff()
            }
        }




    }

    private fun makeCall() {
        val permission = Manifest.permission.CALL_PHONE
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(callIntent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
    }
    private fun endCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use TelecomManager to end the call (API level 28 and above)
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ANSWER_PHONE_CALLS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                telecomManager.endCall()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ANSWER_PHONE_CALLS),
                    END_CALL_PERMISSION_REQUEST_CODE
                )
            }
        } else  {
            // Handle end call for API levels below 28
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val endCallMethod = telephonyManager.javaClass.getDeclaredMethod("endCall")
                endCallMethod.invoke(telephonyManager)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    END_CALL_PERMISSION_REQUEST_CODE
                )
            }
        }
    }




    private fun mute() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = true
    }

    private fun unmute() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = false
    }

    private fun speakerOn() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
    }

    private fun speakerOff() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
    }
}