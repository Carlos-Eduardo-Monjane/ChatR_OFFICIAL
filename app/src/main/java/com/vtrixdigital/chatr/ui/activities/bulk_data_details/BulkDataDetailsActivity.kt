package com.vtrixdigital.chatr.ui.activities.bulk_data_details

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtrixdigital.chatr.adapters.BulkDataDetailsAdapter
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.ActivityBulkDataDetailsBinding
import com.vtrixdigital.chatr.ui.activities.BulkMessagingActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BulkDataDetailsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityBulkDataDetailsBinding
    lateinit var viewModel: BulkDataDetailsViewModel
    private val recordListAdapter = BulkDataDetailsAdapter(arrayListOf())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val campaignName = intent.getStringExtra("campaignName").toString()
        val source = intent.getStringExtra("source").toString()

        if (campaignName.isEmpty() || source.isEmpty()) {
            startActivity(Intent(this, BulkMessagingActivity::class.java))
            finish()
        }

        binding = ActivityBulkDataDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this).get(BulkDataDetailsViewModel::class.java)

        db = DatabaseHelper().getInstance(this@BulkDataDetailsActivity)
        viewModel.refresh(db, campaignName, source)
        binding.usersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordListAdapter
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.records?.observe(this, { users ->
            users?.let {
                recordListAdapter.updateRecords(it)
                if (users.isNotEmpty()) {
                    binding.animationView.visibility = View.GONE
                    binding.usersList.visibility = View.VISIBLE
                } else {
                    binding.animationView.visibility = View.VISIBLE
                    binding.usersList.visibility = View.GONE
                }
            }
        })
    }
}