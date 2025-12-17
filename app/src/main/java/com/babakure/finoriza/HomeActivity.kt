package com.babakure.finoriza

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import com.google.android.material.snackbar.Snackbar

class HomeActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var adapter: WishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersiveFullscreen()
        setContentView(R.layout.activity_home)

        prefs = Prefs(this)

        findViewById<View>(R.id.btnCreate).setOnClickListener {
            startActivity(Intent(this, WishFormActivity::class.java))
        }

//        findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
//            startActivity(Intent(this, AllWishesActivity::class.java))
//        }


        val rv = findViewById<RecyclerView>(R.id.recyclerLatest)
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
                val list = prefs.load().toMutableList()
                val item = list.first { it.id == w.id }
                item.status = WishStatus.ARCHIVED
                item.ops.add(Op(OpType.ARCHIVE, null, System.currentTimeMillis()))
                prefs.save(list)
                reload()
            },
            onPurchased = { w ->
                val list = prefs.load().toMutableList()
                val item = list.first { it.id == w.id }
                if (item.current < item.goal) item.current = item.goal
                item.status = WishStatus.PURCHASED
                item.ops.add(Op(OpType.PURCHASE, null, System.currentTimeMillis()))
                prefs.save(list)
                reload()
            },
            onDelete = { w ->
                AlertDialog.Builder(this)
                    .setMessage("İsteği kalıcı olarak silmek?")
                    .setPositiveButton("Sil") { _, _ ->
                        val list = prefs.load().toMutableList()
                        list.removeAll { it.id == w.id }
                        prefs.save(list)
                        reload()
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        )
        rv.adapter = adapter
        val layoutLatestSeeAll = findViewById<LinearLayout>(R.id.layoutLatestSeeAll)

        layoutLatestSeeAll.setOnClickListener {
            startActivity(Intent(this, AllWishesActivity::class.java))
        }
        onBackPressedDispatcher.addCallback(this) { }

        reload()
    }

    private fun reload() {
        val all = prefs.load().filter { it.status == WishStatus.ACTIVE }
        val latest = all.sortedByDescending { it.createdAt }.take(5)
        adapter.submit(latest)

        if (latest.isEmpty() ) {
            showAddDataSnackbar()
        }

    }

    override fun onResume() {
        super.onResume()
        immersiveFullscreen()
        reload()
    }

    fun showAddDataSnackbar(duration: Int = Snackbar.LENGTH_LONG) {
        val root = findViewById<View>(android.R.id.content)
        Snackbar
            .make(root, "Veri ekleyin", duration)
            .show()
    }
}