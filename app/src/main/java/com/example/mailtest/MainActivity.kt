package com.example.mailtest
//final modificatinon by lingaa
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
    private var isTTSInitialized = false
    private var hasReadEmails = false

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

                if (!hasReadEmails) {
                    hasReadEmails = true
                    readEmailsSummary()
                }
            }
        }.start()
    }

    private fun readEmailsSummary() {
        if (!isTTSInitialized) return

        val text = emails.joinToString(" ") {
            "From ${it.sender}. Subject: ${it.subject}. Received on ${it.date}."
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun readSelectedEmail(email: Email) {
        if (!isTTSInitialized) return

        val text = "From ${email.sender}. Subject: ${email.subject}. Received on ${email.date}. Body: ${email.body}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun stopReading() {
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
