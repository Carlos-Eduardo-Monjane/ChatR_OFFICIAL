package com.vtrixdigital.chatr.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.database.BulkImportList
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.BulkDetailsDataRowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BulkDataDetailsAdapter (var records: ArrayList<BulkImportList>): RecyclerView.Adapter<BulkDataDetailsAdapter.RecordsViewHolder>() {
    fun updateRecords(newRecords: List<BulkImportList>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) : RecordsViewHolder {
        val binding = BulkDetailsDataRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordsViewHolder(binding)
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.bind(records[position])
    }

    class RecordsViewHolder(private val binding: BulkDetailsDataRowBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(records: BulkImportList) {
            binding.dateTime.text = records.createdAt
            binding.receiverName.text = records.receiver
            binding.message.text = records.message
            binding.lastSentTime.text = records.sentTime

            when (records.appName) {
                binding.root.context.getString(R.string.whatsapp_package) -> binding.appName.text = "Whatsapp"
                binding.root.context.getString(R.string.whatsapp_business_package) -> binding.appName.text = "Whatsapp Business"
                binding.root.context.getString(R.string.gbwhatsapp_package) -> binding.appName.text = "GBWhatsapp"
                else -> binding.appName.text = "Whatsapp"
            }

            if(records.isSent) {
                binding.isSent.background = binding.root.context.getDrawable(R.drawable.ic_baseline_check_circle_24_green)
                binding.lastSentTime.text = "Last Sent On : ${records.sentTime}"
            }else{
                binding.isSent.background = binding.root.context.getDrawable(R.drawable.ic_baseline_check_circle_24)
                binding.lastSentTime.text = "Not scheduled"
            }

            binding.deleteBtn.setOnClickListener{
                val alertDialog: AlertDialog = binding.root.context.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("OK"
                        ) { dialog, id ->
                            deleteFromDb(binding.root.context, records.id)
                        }
                        setNegativeButton(R.string.cancel
                        ) { dialog, id ->
                            // User cancelled the dialog
                        }
                    }
                    builder.setTitle("Are you sure you want to delete this record?")
                    builder.create()
                }
                alertDialog.show()
            }
        }

        private fun deleteFromDb(context: Context?, id: Int?) {
            GlobalScope.launch(Dispatchers.IO) {
                if (id != null) {
                    val db = context?.let { DatabaseHelper().getInstance(it) }
                    db?.bulkImportDao()?.deleteQueueById(id)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context , "Delete successfully!" , Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context , "Error while deleting!" , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}