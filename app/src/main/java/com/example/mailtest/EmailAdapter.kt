package com.example.mailtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mailtest.model.Email

class EmailAdapter(
    private val emailList: List<Email>,
    private val onClick: (Email) -> Unit
) : RecyclerView.Adapter<EmailAdapter.EmailViewHolder>() {

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
        holder.itemView.setOnClickListener { onClick(email) }
    }

    override fun getItemCount() = emailList.size
}
