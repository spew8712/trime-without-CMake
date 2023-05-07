package com.osfans.trime.settings

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.BarUtils
import com.osfans.trime.R
import com.osfans.trime.data.Config
import com.osfans.trime.data.db.DbDao
import com.osfans.trime.databinding.LiquidKeyboardClipActivityBinding
import com.osfans.trime.ime.symbol.CheckableAdatper
import com.osfans.trime.ime.symbol.SimpleKeyBean
import timber.log.Timber

class LiquidKeyboardClipActivity : AppCompatActivity() {
        lateinit var dbName: String
        lateinit var binding: LiquidKeyboardClipActivityBinding
        var beans: List<SimpleKeyBean> = ArrayList()
        lateinit var mAdapter: CheckableAdatper
        override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = LiquidKeyboardClipActivityBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        BarUtils.setNavBarColor(
        this,
        getColor(R.color.windowBackground)
        )
        } else
        BarUtils.setNavBarColor(
        this,
@Suppress("DEPRECATION")
                resources.getColor(R.color.windowBackground)
                        )
                        setContentView(binding.root)

                        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbName = intent.getStringExtra("type").toString()
        if (dbName.equals(DbDao.COLLECTION)) {
        setTitle(R.string.other__list_collection_title)
        } else if (dbName.equals(DbDao.DRAFT)) {
        setTitle(R.string.other__list_draft_title)
        }else if (dbName.equals(DbDao.CLIP)) {
        setTitle(R.string.other__list_clip_title)
        } else {
        dbName = DbDao.CLIPBOARD
        setTitle(R.string.other__list_clipboard_title)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getDbData()

        binding.btnDel.setOnClickListener {

        var position: Int =
        binding.listWord.getPositionForView(binding.listWord.getChildAt(0))
        if (position > 0) position++
        mAdapter.remove(position)

        binding.btnEdit.visibility = View.VISIBLE
        binding.btnMerge.visibility = View.GONE

        Timber.d("delete")
        }

        binding.btnReset.setOnClickListener {
        Timber.d("reset")
        mAdapter.resetClips()
        }

        binding.btnEdit.setOnClickListener {

        var title = R.string.edit
        val et = EditText(this)
        val checked = mAdapter.checked.size
        if (checked> 0) {
        et.setText(mAdapter.checkedText)
        } else {
        title = R.string.add
        }
        AlertDialog.Builder(this)
        .setTitle(title)
        .setView(et)
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok) { _, _ ->
        mAdapter.updateItem(et.text.toString())
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
        }

        binding.btnMerge.setOnClickListener {
        Timber.d("merge")
        mAdapter.mergeItem()
        }
        }

        override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(FRAGMENT_TAG, title)
        }

        override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
        return true
        }
        return super.onSupportNavigateUp()
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
        android.R.id.home -> {
        onBackPressed()
        true
        }
        else -> super.onOptionsItemSelected(item)
        }
        }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
        }

        fun getDbData() {
        binding.progressBar.visibility = View.VISIBLE

        if (dbName.equals(DbDao.COLLECTION)) {
        beans = DbDao(dbName).getAllSimpleBean(-1, 0)
        binding.btnReset.visibility = View.GONE
        } else if (dbName.equals(DbDao.DRAFT)) {
        beans = DbDao(dbName).getAllSimpleBean(1000, Config.get(this).draftTimeOut)
        } else if (dbName.equals(DbDao.CLIP)) {
        beans = DbDao(dbName).getAllSimpleBean(-1,0)
        } else {
        dbName = DbDao.CLIPBOARD
        beans = DbDao(dbName).getAllSimpleBean(1000, Config.get(this).clipboardTimeOut)
        }

        mAdapter = CheckableAdatper(
        this,
        R.layout.checkable_item,
        beans,
        dbName
        )
        binding.listWord.adapter = mAdapter

        binding.listWord.setOnItemClickListener({ parent: AdapterView<*>?, view: View?, i: Int, l: Long ->
        mAdapter.clickItem(
        i
        )
        val selected = mAdapter.checked.size
        if (selected> 1) {
        binding.btnEdit.visibility = View.GONE
        binding.btnMerge.visibility = View.VISIBLE
        } else {
        binding.btnEdit.visibility = View.VISIBLE
        binding.btnMerge.visibility = View.GONE
        }
        })
        binding.progressBar.visibility = View.GONE
        }
        }
