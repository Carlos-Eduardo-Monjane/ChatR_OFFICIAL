package com.vtrixdigital.chatr.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.bulk_messaging_utilities.WASenderFgSvc
import com.vtrixdigital.chatr.database.BulkImportList
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.SendViaCsvRowBinding
import com.vtrixdigital.chatr.ui.activities.bulk_data_details.BulkDataDetailsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SendViaCsvAdapter(var records: ArrayList<BulkImportList>): RecyclerView.Adapter<SendViaCsvAdapter.RecordsViewHolder>() {
    fun updateRecords(newRecords: List<BulkImportList>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) : RecordsViewHolder {
        val binding = SendViaCsvRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordsViewHolder(binding)
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.bind(records[position])
    }

    class RecordsViewHolder(private val binding: SendViaCsvRowBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(records: BulkImportList) {
            if(records.messageType == "" || records.messageType.isNullOrBlank()){
                binding.messageType.text = "Text"
            }else
                binding.messageType.text = records.messageType

            binding.dateTime.text = records.createdAt
            binding.campaignName.text = records.campaignName

            if(records.isSent){
                binding.lastSentTime.text = "Last Sent On : ${records.sentTime}"
                binding.start.visibility = View.GONE
            }else{
                binding.lastSentTime.text = "Not scheduled"
                binding.start.visibility = View.VISIBLE
                binding.start.setOnClickListener{
                    startMessaging(records.campaignName , records.source , records.messageType, binding.root.context)
                }
            }
            when (records.appName) {
                binding.root.context.getString(R.string.whatsapp_package) -> binding.appName.text = "Whatsapp"
                binding.root.context.getString(R.string.whatsapp_business_package) -> binding.appName.text = "Whatsapp Business"
                binding.root.context.getString(R.string.gbwhatsapp_package) -> binding.appName.text = "GBWhatsapp"
                else -> binding.appName.text = "Whatsapp"
            }
            binding.mainRootElement.setOnClickListener{
                 val intent = Intent(binding.root.context, BulkDataDetailsActivity::class.java)
                 intent.putExtra("campaignName" , records.campaignName)
                 intent.putExtra("source" , records.source)
                 binding.root.context.startActivity(intent)
            }
            binding.deleteBtn.setOnClickListener{
                deleteCsvList(binding.root.context , records.campaignName ,records.source , records.appName)
            }
        }

        private fun deleteCsvList(context: Context,campaignName: String, source: String, appName: String) {
            val alertDialog: AlertDialog = context.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("OK"
                    ) { dialog, id ->
                        deleteFromDb(context, campaignName, source, appName)
                    }
                    setNegativeButton(R.string.cancel
                    ) { dialog, id ->
                        // User cancelled the dialog
                    }
                }
                builder.setTitle("Are you sure you want to delete this campaign?")
                builder.create()
            }
            alertDialog.show()
        }

        private fun deleteFromDb(context: Context,campaignName: String, source: String, appName: String) {
            GlobalScope.launch(Dispatchers.IO) {
                val db = DatabaseHelper().getInstance(context)
                db.bulkImportDao().deleteUploadedQueue(campaignName , source , appName)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context , "Delete successfully!" , Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun startMessaging(campaignName: String , source: String, messageType : String?, context: Context) {
            var messageTypeString = messageType
            if(messageTypeString == "" || messageTypeString.isNullOrBlank()){
                messageTypeString = "text"
            }
            Toast.makeText(context , "Starting! Please wait" , Toast.LENGTH_SHORT).show()
            val intent = Intent(context, WASenderFgSvc::class.java)
            intent.putExtra("start" , true)
            intent.putExtra("source" , source)
            intent.putExtra("campaignName" , campaignName)
            intent.putExtra("messageType" , messageTypeString)
            context.startService(intent)
        }
    }
}