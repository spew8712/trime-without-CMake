package com.osfans.trime.setup

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.NotificationUtils
import com.osfans.trime.R
import com.osfans.trime.databinding.ActivitySetupBinding
import com.osfans.trime.setup.SetupPage.Companion.firstUndonePage
import com.osfans.trime.setup.SetupPage.Companion.isLastPage
import timber.log.Timber

class SetupActivity : FragmentActivity() {
    private lateinit var viewPager: ViewPager2
    private val viewModel: SetupViewModel by viewModels()

    companion object {
        private var binaryCount = false
        private const val NOTIFY_ID = 87463

        fun shouldSetup() = !binaryCount && SetupPage.hasUndonePage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prevButton = binding.prevButton.apply {
            text = getString(R.string.setup__prev)
            setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
        }
        binding.skipButton.apply {
            text = getString(R.string.setup__skip)
            setOnClickListener {
                AlertDialog.Builder(this@SetupActivity)
                    .setMessage(R.string.setup__skip_hint)
                    .setPositiveButton(R.string.setup__skip_hint_yes) { _, _ ->
                        finish()
                    }
                    .setNegativeButton(R.string.setup__skip_hint_no, null)
                    .show()
            }
        }
        val nextButton = binding.nextButton.apply {
            setOnClickListener {
                if (viewPager.currentItem != SetupPage.values().size - 1) {
                    viewPager.currentItem = viewPager.currentItem + 1
                } else finish()
            }
        }
        viewPager = binding.viewpager
        viewPager.adapter = Adapter()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Manually call following observer when page changed
                // intentionally before changing the text of nextButton
                viewModel.isAllDone.value = viewModel.isAllDone.value
                // Hide prev button for the first page
                prevButton.visibility = if (position != 0) View.VISIBLE else View.GONE
                nextButton.text =
                    getString(
                        if (position.isLastPage())
                            R.string.busy_progress else R.string.setup__next
                    )
            }
        })
        viewModel.isAllDone.observe(this) { allDone ->
            nextButton.apply {
                // Hide next button for the last page when allDone == false
                val notLastPage = !viewPager.currentItem.isLastPage()
                Timber.i("Setup page allDone=$allDone, isLastPage=$notLastPage")
                if (allDone)
                    finish()

                (allDone || notLastPage).let {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }
        }
        // Skip to undone page
        firstUndonePage()?.let { viewPager.currentItem = it.ordinal }
        binaryCount = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        (fragment as SetupFragment).sync()
    }

    override fun onPause() {
        if (SetupPage.hasUndonePage()) {
            NotificationUtils.notify(NOTIFY_ID) { param ->
                param.setSmallIcon(R.drawable.ic_status)
                    .setContentTitle(getText(R.string.trime_app_name))
                    .setContentText(getText(R.string.setup__notify_hint))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            this,
                            0,
                            Intent(this, javaClass),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .setAutoCancel(true)
            }
        }
        super.onPause()
    }

    override fun onResume() {
        NotificationUtils.cancel(NOTIFY_ID)
        super.onResume()
    }

    private inner class Adapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = SetupPage.values().size

        override fun createFragment(position: Int): Fragment =
            SetupFragment().apply {
                arguments = bundleOf("page" to SetupPage.values()[position])
            }
    }
}
