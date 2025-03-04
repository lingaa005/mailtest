package com.example.mailtest

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmailDetailActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_detail)

        // Get the views
        val senderTextView = findViewById<TextView>(R.id.sender)
        val subjectTextView = findViewById<TextView>(R.id.subject)
        val bodyTextView = findViewById<TextView>(R.id.body)

        // Retrieve data safely
        val sender = intent.getStringExtra("sender") ?: "Unknown Sender"
        val subject = intent.getStringExtra("subject") ?: "No Subject"
        val body = intent.getStringExtra("body") ?: "No Content"

        // Set text to views
        senderTextView.text = "From: $sender"
        subjectTextView.text = "Subject: $subject"
        bodyTextView.text = body
    }
}
