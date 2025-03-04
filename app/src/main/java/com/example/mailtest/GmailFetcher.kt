package com.example.mailtest

import android.util.Log
import com.example.mailtest.model.Email
import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPStore
import java.util.*
import javax.mail.*
import javax.mail.event.MessageCountAdapter
import javax.mail.event.MessageCountEvent

object GmailFetcher {
    private const val IMAP_HOST = "imap.gmail.com"
    private const val IMAP_PORT = "993"
    private const val EMAIL = "kasilingamtvm@gmail.com" // Replace with actual email
    private const val PASSWORD = "eaud rrjj mqzf gtmt" // Use an App Password (not direct password)

    private var store: IMAPStore? = null
    private var inbox: IMAPFolder? = null
    private var isListening = false

    fun startListeningForEmails(onNewEmail: (List<Email>) -> Unit) {
        if (isListening) return // Prevent duplicate listeners

        Thread {
            try {
                val props = Properties().apply {
                    put("mail.store.protocol", "imaps")
                    put("mail.imaps.host", IMAP_HOST)
                    put("mail.imaps.port", IMAP_PORT)
                    put("mail.imaps.ssl.enable", "true")
                }

                val session = Session.getInstance(props)
                store = session.getStore("imaps") as IMAPStore
                store?.connect(IMAP_HOST, EMAIL, PASSWORD)

                inbox = store?.getFolder("INBOX") as IMAPFolder
                inbox?.open(Folder.READ_ONLY)

                inbox?.addMessageCountListener(object : MessageCountAdapter() {
                    override fun messagesAdded(event: MessageCountEvent?) {
                        event?.messages?.let { messages ->
                            val emailList = messages.map { message ->
                                Email(
                                    subject = message.subject ?: "No Subject",
                                    sender = message.from?.firstOrNull()?.toString() ?: "Unknown Sender",
                                    body = message.content.toString(),
                                    date = message.sentDate.toString()
                                )
                            }
                            onNewEmail(emailList)
                        }
                    }
                })

                isListening = true

                // Keep the connection alive using IMAP IDLE
                while (isListening) {
                    if (inbox is IMAPFolder) {
                        Log.d("GmailFetcher", "Waiting for new messages...")
                        (inbox as IMAPFolder).idle()
                    }
                }
            } catch (e: Exception) {
                Log.e("GmailFetcher", "Error in email listener: ${e.message}", e)
            }
        }.start()
    }

    fun stopListening() {
        isListening = false
        try {
            inbox?.close(false)
            store?.close()
        } catch (e: MessagingException) {
            Log.e("GmailFetcher", "Error closing IMAP connection: ${e.message}", e)
        }
    }
}
