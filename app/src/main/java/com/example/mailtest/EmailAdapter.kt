package com.example.mailtest

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mailtest.model.Email
import java.util.Locale

class EmailAdapter(
    private val context: Context,
    private val emailList: List<Email>,
    private val onClick: (Email) -> Unit
) : RecyclerView.Adapter<EmailAdapter.EmailViewHolder>(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isReadingAll = false  // Ensures reading all emails only happens once

    class EmailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.email_sender)
        val subject: TextView = view.findViewById(R.id.email_subject)
        val date: TextView = view.findViewById(R.id.email_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_email, parent, false)
        return EmailViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = emailList[position]
        holder.sender.text = email.sender
        holder.subject.text = email.subject
        holder.date.text = email.date

        // Click listener to stop current TTS and read selected email
        holder.itemView.setOnClickListener {
            stopReading()
            readEmail(email)
            onClick(email)
        }

        // Read all emails only once when first loaded
        if (position == 0 && !isReadingAll) {
            isReadingAll = true
            readAllEmails()
        }
    }

    override fun getItemCount() = emailList.size

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
        }
    }

    // Reads all emails in order, one by one
    private fun readAllEmails() {
        Thread {
            for (email in emailList) {
                readEmail(email)
                Thread.sleep(3000)  // Delay to prevent overlap (adjust as needed)
            }
        }.start()
    }

    // Reads a specific email aloud
    private fun readEmail(email: Email) {
        val text = "From ${email.sender}, Subject: ${email.subject}, Date: ${email.date}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Stops any ongoing speech
    private fun stopReading() {
        if (tts.isSpeaking) {
            tts.stop()
        }
    }

    fun shutdownTTS() {
        tts.stop()
        tts.shutdown()
    }
}
