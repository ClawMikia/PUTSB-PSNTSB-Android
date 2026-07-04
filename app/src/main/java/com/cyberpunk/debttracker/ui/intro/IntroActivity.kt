package com.cyberpunk.debttracker.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.databinding.ActivityIntroBinding
import com.cyberpunk.debttracker.ui.dashboard.MainActivity
import com.google.android.material.tabs.TabLayoutMediator

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pages = listOf(
            IntroPage(
                R.string.intro_title_1,
                R.string.intro_desc_1,
                R.drawable.splash_animated_icon
            ),
            IntroPage(
                R.string.intro_title_2,
                R.string.intro_desc_2,
                R.drawable.ic_dashboard
            ),
            IntroPage(
                R.string.intro_title_3,
                R.string.intro_desc_3,
                R.drawable.ic_analytics
            ),
            IntroPage(
                R.string.intro_title_4,
                R.string.intro_desc_4,
                R.drawable.ic_archive
            )
        )

        val adapter = IntroPagerAdapter(pages)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pages.size - 1) {
                    binding.btnNext.text = getString(R.string.btn_get_started)
                } else {
                    binding.btnNext.text = getString(R.string.btn_next)
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < pages.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishIntro()
            }
        }
    }

    private fun finishIntro() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean("is_first_run", false)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

data class IntroPage(
    val titleRes: Int,
    val descRes: Int,
    val iconRes: Int
)

class IntroPagerAdapter(private val pages: List<IntroPage>) :
    RecyclerView.Adapter<IntroPagerAdapter.IntroViewHolder>() {

    class IntroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIntro: ImageView = view.findViewById(R.id.ivIntro)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_intro_page, parent, false)
        return IntroViewHolder(view)
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        val page = pages[position]
        holder.tvTitle.setText(page.titleRes)
        holder.tvDescription.setText(page.descRes)
        holder.ivIntro.setImageResource(page.iconRes)
    }

    override fun getItemCount() = pages.size
}
