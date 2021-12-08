package com.vtrixdigital.chatr.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.database.SentMessages
import com.vtrixdigital.chatr.databinding.SentMessagesBinding
import com.vtrixdigital.chatr.ui.activities.live_chat.LiveChatActivity

class SentMessageAdapter(var records: ArrayList<SentMessages>): RecyclerView.Adapter<SentMessageAdapter.RecordsViewHolder>() {
    fun updateRecords(newRecords: List<SentMessages>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) : RecordsViewHolder {
        val binding = SentMessagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordsViewHolder(binding)
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.bind(records[position])
    }

    class RecordsViewHolder(private val binding: SentMessagesBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(records: SentMessages) {
            binding.dateTime.text = records.createdAt
            binding.receiverName.text = records.sender
            binding.lastSentMessage.text = records.replyMessage
            binding.customerImage.text = binding.root.context.getString(R.string.customer_image ,records.sender.first().toUpperCase() , records.sender.last().toUpperCase())

            when {
                records.appName == binding.root.context.getString(R.string.keyWhatsapp) -> binding.appName.text = binding.root.context.getString(R.string.whatsapp)
                records.appName == binding.root.context.getString(R.string.keyWhatsappBusiness) -> binding.appName.text = binding.root.context.getString(R.string.whatsapp_business)
                records.appName == binding.root.context.getString(R.string.keyGBWhatsapp) -> binding.appName.text = binding.root.context.getString(R.string.gb_whatsapp)
                records.appName == binding.root.context.getString(R.string.keyInstagram) -> binding.appName.text = binding.root.context.getString(R.string.instagram)
                records.appName == binding.root.context.getString(R.string.keyFbMessenger) -> binding.appName.text = binding.root.context.getString(R.string.messenger)
                records.appName == binding.root.context.getString(R.string.keyTelegram) -> binding.appName.text = binding.root.context.getString(R.string.telegram)
                records.appName.contains(binding.root.context.getString(R.string.whatsapp)) -> binding.appName.text = binding.root.context.getString(R.string.whatsapp_plus)
                else -> binding.appName.text = binding.root.context.getString(R.string.whatsapp)
            }

            binding.customerView.setOnClickListener{
//                Navigation.findNavController(binding.customerView).navigate(R.id.nav_home)
                val intent = Intent(binding.root.context , LiveChatActivity::class.java)
                intent.putExtra("sender" , records.sender)
                binding.root.context.startActivity(intent)
            }
        }
    }
}