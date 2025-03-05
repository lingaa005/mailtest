package com.example.mailtest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailtest.model.Email
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var emailAdapter: EmailAdapter
    private val emails = mutableListOf<Email>()
    private lateinit var tts: TextToSpeech
    private var isReadingAllEmails = false
    private var isTTSInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emailAdapter = EmailAdapter(this, emails) { email ->
            stopReading()
            readSelectedEmail(email)
            openEmail(email)
        }
        recyclerView.adapter = emailAdapter

        fetchEmails()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchEmails() {
        Thread {
            val fetchedEmails = GmailFetcher.fetchEmails()
            Handler(Looper.getMainLooper()).post {
                emails.clear()
                emails.addAll(fetchedEmails)
                emailAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Emails Loaded", Toast.LENGTH_SHORT).show()

                // Start reading emails aloud only once when loaded
                if (!isReadingAllEmails) {
                    isReadingAllEmails = true
                    readAllEmailsAloud()
                }
            }
        }.start()
    }

    private fun readAllEmailsAloud() {
        if (!isTTSInitialized) return

        Thread {
            for (email in emails) {
                if (!isReadingAllEmails) return@Thread  // Stop if user interrupts
                val text = "From ${email.sender}. Subject: ${email.subject}. Received on ${email.date}."
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                Thread.sleep(4000) // Allow enough time to read
            }
            isReadingAllEmails = false // Mark reading as finished
        }.start()
    }

    private fun readSelectedEmail(email: Email) {
        if (!isTTSInitialized) return

        val text = "From ${email.sender}. Subject: ${email.subject}. Received on ${email.date}. Body: ${email.body}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun stopReading() {
        isReadingAllEmails = false
        tts.stop()
    }

    private fun openEmail(email: Email) {
        val intent = Intent(this, EmailDetailActivity::class.java).apply {
            putExtra("subject", email.subject)
            putExtra("sender", email.sender)
            putExtra("body", email.body)
            putExtra("date", email.date)
        }
        startActivity(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            isTTSInitialized = true
        }
    }

    override fun onDestroy() {
        stopReading()
        tts.shutdown()
        super.onDestroy()
    }
}
