package com.example.mailtest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailtest.model.Email
import kotlin.concurrent.thread
class MainActivity : AppCompatActivity() {

    private lateinit var emailAdapter: EmailAdapter
    private val emails = mutableListOf<Email>()
    private val handler = Handler(Looper.getMainLooper())

    private val fetchEmailsRunnable = object : Runnable {
        override fun run() {
            thread {
                val fetchedEmails = GmailFetcher.fetchEmails()

                runOnUiThread {
                    updateEmailList(fetchedEmails)
                }
            }
            handler.postDelayed(this, 2000) // Run again after 2 seconds
        }
    }

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

        // Start auto-fetching emails every 2 seconds
        handler.post(fetchEmailsRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(fetchEmailsRunnable) // Stop auto-fetching when activity is destroyed
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateEmailList(newEmails: List<Email>) {
        val oldSize = emails.size
        val newSize = newEmails.size

        // Compare and update only the changed parts
        if (newSize > oldSize) {
            val addedEmails = newEmails.subList(oldSize, newSize)
            emails.addAll(addedEmails)
            emailAdapter.notifyItemRangeInserted(oldSize, addedEmails.size)
        } else if (newSize < oldSize) {
            emails.clear()
            emails.addAll(newEmails)
            emailAdapter.notifyDataSetChanged() // Only needed if emails are removed
        } else {
            // Update only modified emails
            for (i in newEmails.indices) {
                if (emails[i] != newEmails[i]) {
                    emails[i] = newEmails[i]
                    emailAdapter.notifyItemChanged(i)
                }
            }
        }

        Toast.makeText(this@MainActivity, "Emails Updated", Toast.LENGTH_SHORT).show()
    }
}
