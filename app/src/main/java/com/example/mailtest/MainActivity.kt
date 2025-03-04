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
import android.content.Context
import androidx.appcompat.app.AlertDialog
fun showWelcomeMessage(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Welcome")
        .setMessage("Welcome to our app! Enjoy your experience.")
        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        .show()
}
class MainActivity : AppCompatActivity() {

    private lateinit var emailAdapter: EmailAdapter
    private val emails = mutableListOf<Email>()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        showWelcomeMessage(this)
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

        fetchEmails()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchEmails() {
        thread {
            val fetchedEmails = GmailFetcher.fetchEmails()
            Handler(Looper.getMainLooper()).post {
                emails.clear()
                emails.addAll(fetchedEmails)
                emailAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Emails Loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
