package com.babakure.finoriza

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllWishesActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var adapter: WishAdapter
    private var showArchived = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersiveFullscreen()
        setContentView(R.layout.activity_all_wishes)

        prefs = Prefs(this)

        findViewById<View>(R.id.btnAdd).setOnClickListener {
            startActivity(Intent(this, WishFormActivity::class.java))
        }

        val tabActive = findViewById<TextView>(R.id.tabActive)
        val tabArchive = findViewById<TextView>(R.id.tabArchive)
        val layoutTabActive = findViewById<LinearLayout>(R.id.layoutTabActive)
        val layoutTabArchive = findViewById<LinearLayout>(R.id.layoutTabArchive)

        val switchTab: (Boolean) -> Unit = { archived ->
            showArchived = archived
            renderTabs(tabActive, tabArchive)
            reload()
        }
        tabActive.setOnClickListener { switchTab(false) }
        layoutTabActive.setOnClickListener { switchTab(false) }
        tabArchive.setOnClickListener { switchTab(true) }
        layoutTabArchive.setOnClickListener { switchTab(true) }
        renderTabs(tabActive, tabArchive)
        onBackPressedDispatcher.addCallback(this) { }
        val rv = findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = WishAdapter(
            onDeposit = { w ->
                val i = Intent(this, AmountChangeActivity::class.java)
                i.putExtra("wishId", w.id)
                startActivity(i)
            },
            onEdit = { w ->
                val i = Intent(this, WishFormActivity::class.java)
                i.putExtra("wishId", w.id)
                startActivity(i)
            },
            onArchive = { w ->
                AlertDialog.Builder(this)
                    .setMessage("Arşive taşımak?")
                    .setPositiveButton("Evet") { _, _ ->
                        w.status = WishStatus.ARCHIVED
                        w.ops.add(Op(OpType.ARCHIVE, null, System.currentTimeMillis()))
                        saveAndReload(updatedWish = w)
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            },
            onPurchased = { w ->
                AlertDialog.Builder(this)
                    .setMessage("Satın alındı olarak işaretlensin mi?")
                    .setPositiveButton("Evet") { _, _ ->
                        if (w.current < w.goal) w.current = w.goal
                        w.status = WishStatus.PURCHASED
                        w.ops.add(Op(OpType.PURCHASE, null, System.currentTimeMillis()))
                        saveAndReload(updatedWish = w)
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            },
            onDelete = { w ->
                AlertDialog.Builder(this)
                    .setMessage("İsteği kalıcı olarak silmek?")
                    .setPositiveButton("Sil") { _, _ ->
                        saveAndReload(deleteId = w.id)
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        )
        rv.adapter = adapter

        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            // our protocol says system back disabled; go to preloader/finish
            finish()
        }
    }

    private fun renderTabs(tabActive: TextView, tabArchive: TextView) {
        tabActive.isSelected = !showArchived
        tabArchive.isSelected = showArchived
        tabActive.paint.isUnderlineText = !showArchived
        tabArchive.paint.isUnderlineText = showArchived
    }

    private fun reload() {
        val list = prefs.load().filter { if (showArchived) it.status != WishStatus.ACTIVE else it.status == WishStatus.ACTIVE }
        adapter.submit(list)
        // small fade animation
        val rv = findViewById<RecyclerView>(R.id.recycler)
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = 200
        rv.startAnimation(anim)
    }

    private fun saveAndReload(updatedWish: Wish? = null, deleteId: String? = null) {
        val list = prefs.load().toMutableList()
        if (updatedWish != null) {
            val idx = list.indexOfFirst { it.id == updatedWish.id }
            if (idx != -1) {
                list[idx] = updatedWish
            }
        }
        if (deleteId != null) {
            list.removeAll { it.id == deleteId }
        }
        prefs.save(list)
        reload()
    }

    override fun onResume() {
        super.onResume()
        immersiveFullscreen()
        reload()
    }
}