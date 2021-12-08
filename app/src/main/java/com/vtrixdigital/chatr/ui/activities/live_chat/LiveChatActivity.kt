package com.vtrixdigital.chatr.ui.activities.live_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtrixdigital.chatr.adapters.LiveChatAdapter
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.ActivityLiveChatActivtyBinding
import com.vtrixdigital.chatr.ui.activities.MainActivity

class LiveChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveChatActivtyBinding
    lateinit var viewModel: LiveChatViewModel
    lateinit var sender : String
    private val recordListAdapter = LiveChatAdapter(arrayListOf())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sender = intent.getStringExtra("sender").toString()
        if (sender.isBlank()) {
            val intent = Intent(this@LiveChatActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivityLiveChatActivtyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(LiveChatViewModel::class.java)
        val db = DatabaseHelper().getInstance(this)
        viewModel.refresh(db , sender)

        binding.usersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordListAdapter
        }

        observeViewModel()
    }
    private fun observeViewModel() {
        viewModel.records?.observe(this, { users ->
            users?.let {
                Log.d("changed" , it.toString())
                recordListAdapter.updateRecords(it)
            }
        })
    }
}