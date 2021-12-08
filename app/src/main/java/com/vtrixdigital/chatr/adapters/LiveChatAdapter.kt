package com.vtrixdigital.chatr.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtrixdigital.chatr.database.SentMessages
import com.vtrixdigital.chatr.databinding.ChatLayoutBinding

class LiveChatAdapter(var records: ArrayList<SentMessages>): RecyclerView.Adapter<LiveChatAdapter.RecordsViewHolder>() {
    fun updateRecords(newRecords: List<SentMessages>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecordsViewHolder {
        val binding =
            ChatLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordsViewHolder(binding)
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.bind(records[position])
    }

    class RecordsViewHolder(private val binding: ChatLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(records: SentMessages) {
            binding.expectedMsgTxt.text = records.receivedMessage
            binding.replyMsgTxt.text = records.replyMessage
        }
    }
}