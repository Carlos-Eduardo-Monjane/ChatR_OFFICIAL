package com.vtrixdigital.chatr.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtrixdigital.chatr.database.Rules
import com.vtrixdigital.chatr.databinding.ItemMessage1Binding
import com.vtrixdigital.chatr.ui.activities.ManageRuleActivity

class ReplyMessageAdapter(private val items: List<Rules>, val context: Context) : RecyclerView.Adapter<ReplyMessageAdapter.ViewHolder>() {

    private lateinit var itemMessage1Binding : ItemMessage1Binding
    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        itemMessage1Binding.expectedMsgTxt.text = items[p1].incoming_message
        itemMessage1Binding.replyMsgTxt.text = items[p1].reply_message
        itemMessage1Binding.root.setOnClickListener{
            editRule(items[p1])
        }
    }

    private fun editRule(rule: Rules) {
        val intent = Intent( context, ManageRuleActivity::class.java)
        intent.putExtra("ruleId" , rule.id)
        intent.putExtra("isUpdating" , true)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        itemMessage1Binding = ItemMessage1Binding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ViewHolder(itemMessage1Binding)
    }

    inner class ViewHolder(itemMessage1Binding1: ItemMessage1Binding) : RecyclerView.ViewHolder(itemMessage1Binding1.root)
}
