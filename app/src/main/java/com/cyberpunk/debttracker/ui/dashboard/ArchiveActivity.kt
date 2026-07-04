package com.cyberpunk.debttracker.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.databinding.ActivityArchiveBinding
import com.cyberpunk.debttracker.ui.debtdetail.DebtDetailActivity
import com.cyberpunk.debttracker.util.SecurityHelper
import com.cyberpunk.debttracker.util.gone
import com.cyberpunk.debttracker.util.visible
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchiveBinding
    private val viewModel: DebtViewModel by viewModels()
    private lateinit var archiveAdapter: DebtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecycler()
        setupClickListeners()

        if (!SecurityHelper.hasArchivePassword(this)) {
            binding.tvLockStatus.text = getString(R.string.archive_encryption_not_set)
            binding.btnUnlock.text = getString(R.string.archive_btn_set_password)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        archiveAdapter = DebtAdapter { debt ->
            startActivity(
                Intent(this, DebtDetailActivity::class.java).apply {
                    putExtra(DebtDetailActivity.EXTRA_DEBT, debt)
                }
            )
        }
        binding.recyclerArchived.apply {
            layoutManager = LinearLayoutManager(this@ArchiveActivity)
            adapter = archiveAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnUnlock.setOnClickListener {
            if (!SecurityHelper.hasArchivePassword(this)) {
                showCreatePasswordDialog()
            } else {
                showEnterPasswordDialog()
            }
        }
    }

    private fun showCreatePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null)
        val title = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val et = dialogView.findViewById<TextInputEditText>(R.id.et_password)
        title.text = getString(R.string.archive_set_password_title)

        AlertDialog.Builder(this, R.style.Theme_DebtTracker_Dialog)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.archive_btn_set_password)) { _, _ ->
                val password = et.text.toString()
                if (password.length >= 4) {
                    SecurityHelper.setArchivePassword(this, password)
                    Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show()
                    showEnterPasswordDialog()
                } else {
                    Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun showEnterPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null)
        val et = dialogView.findViewById<TextInputEditText>(R.id.et_password)

        AlertDialog.Builder(this, R.style.Theme_DebtTracker_Dialog)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.archive_btn_unlock)) { _, _ ->
                val password = et.text.toString()
                if (password == SecurityHelper.getArchivePassword(this)) {
                    unlockArchive()
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun unlockArchive() {
        binding.layoutLocked.gone()
        binding.recyclerArchived.visible()
        observeArchivedDebts()
    }

    private fun observeArchivedDebts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.archivedDebts.collect { debts ->
                    archiveAdapter.submitList(debts)
                    if (debts.isEmpty()) {
                        binding.layoutEmptyArchive.visible()
                        binding.recyclerArchived.gone()
                    } else {
                        binding.layoutEmptyArchive.gone()
                        binding.recyclerArchived.visible()
                    }
                }
            }
        }
    }
}
