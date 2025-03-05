package com.example.mailtest

import com.example.mailtest.model.Email
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress

object GmailFetcher {

    private const val IMAP_HOST = "imap.gmail.com"
    private const val IMAP_PORT = "993"
    private const val USERNAME = "mkasilingammuthu@gmail.com"  // Replace with your email
    private const val APP_PASSWORD = "wiwc ixkd hvjh aruu" // Replace with App Password

    fun fetchEmails(): List<Email> {
        val emails = mutableListOf<Email>()
        try {
            val props = Properties().apply {
                put("mail.store.protocol", "imaps")
                put("mail.imap.host", IMAP_HOST)
                put("mail.imap.port", IMAP_PORT)
                put("mail.imap.ssl.enable", "true")
            }

            val session = Session.getInstance(props, null)
            val store = session.getStore("imaps")
            store.connect(IMAP_HOST, USERNAME, APP_PASSWORD)

            val inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)

            val messages = inbox.messages.reversed().take(20) // Get latest 20 emails
            for (message in messages) {
                val sender = (message.from[0] as InternetAddress).toString()
                val subject = message.subject ?: "No Subject"
                val date = message.sentDate.toString()
                val body = getTextFromMessage(message)

                emails.add(Email(subject, sender, body, date))
            }

            inbox.close(false)
            store.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emails
    }

    private fun getTextFromMessage(message: Message): String {
        return when (val content = message.content) {
            is String -> content
            is Multipart -> (content.getBodyPart(0).content as? String) ?: "Attachment Only"
            else -> "Unknown Content"
        }
    }
}
