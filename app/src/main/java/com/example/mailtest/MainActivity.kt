package com.example.mailtest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailtest.model.Email

class MainActivity : AppCompatActivity() {

    private lateinit var emailAdapter: EmailAdapter
    private val emails = mutableListOf<Email>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        emailAdapter = EmailAdapter(emails) { email ->
            val intent = Intent(this, EmailDetailActivity::class.java).apply {
                putExtra("subject", email.subject)
                putExtra("sender", email.sender)
                putExtra("body", email.body)
                putExtra("date", email.date)
            }
            startActivity(intent)
        }

        recyclerView.adapter = emailAdapter

        // Start listening for new emails
        GmailFetcher.startListeningForEmails { newEmails ->
            runOnUiThread {
                updateEmailList(newEmails)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GmailFetcher.stopListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateEmailList(newEmails: List<Email>) {
        emails.addAll(newEmails)
        emailAdapter.notifyDataSetChanged()
        Toast.makeText(this@MainActivity, "New Email Received!", Toast.LENGTH_SHORT).show()
    }
}
