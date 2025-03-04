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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.example.mailtest.model.Email

class MainActivity : AppCompatActivity() {

    private lateinit var emailAdapter: EmailAdapter
    private val emails = mutableListOf<Email>()
    private val handler = Handler(Looper.getMainLooper())

    // Thread pool limiting to 12 concurrent fetches
    private val executorService = Executors.newFixedThreadPool(12)

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

        // Schedule email fetching every 2 seconds
        startFetchingEmails()
    }

    private fun startFetchingEmails() {
        handler.post(object : Runnable {
            override fun run() {
                executorService.execute {
                    val fetchedEmails = GmailFetcher.fetchEmails()

                    runOnUiThread {
                        updateEmailList(fetchedEmails)
                    }
                }
                handler.postDelayed(this, 2000) // Fetch again after 2 sec
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop auto-fetching
        executorService.shutdown() // Stop thread pool execution
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow() // Force shutdown if tasks are running
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateEmailList(newEmails: List<Email>) {
        val previousSize = emails.size

        // Find new emails
        val newEntries = newEmails.filter { it !in emails }
        emails.addAll(newEntries)

        // Remove old emails
        emails.retainAll(newEmails)

        if (emails.size != previousSize) {
            emailAdapter.notifyDataSetChanged()
            Toast.makeText(this@MainActivity, "Emails Updated", Toast.LENGTH_SHORT).show()
        }
    }
}
