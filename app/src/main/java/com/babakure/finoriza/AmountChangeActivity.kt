package com.babakure.finoriza

import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar

class AmountChangeActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var wish: Wish
    private var isIncome = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersiveFullscreen()
        setContentView(R.layout.activity_amount_change)
        prefs = Prefs(this)

        val wishId = intent.getStringExtra("wishId")!!
        wish = prefs.load().first { it.id == wishId }

        val tvIncome = findViewById<TextView>(R.id.tvIncome)
        val tvExpense = findViewById<TextView>(R.id.tvExpense)
        val input = findViewById<EditText>(R.id.inputAmount)
        val btn = findViewById<Button>(R.id.btnApply)

        input.filters = arrayOf(InputFilter.LengthFilter(6))

        fun updateToggle() {
            val blue = 0xFF1A69FF.toInt()
            val red = 0xFFD32F2F.toInt()
            val gray = 0xFF9AA2A8.toInt()
            tvIncome.setTextColor(if (isIncome) blue else gray)
            tvExpense.setTextColor(if (!isIncome) red else gray)
            tvIncome.paint.isUnderlineText = isIncome
            tvExpense.paint.isUnderlineText = !isIncome
        }
        tvIncome.setOnClickListener { isIncome = true; updateToggle() }
        tvExpense.setOnClickListener { isIncome = false; updateToggle() }
        updateToggle()

        btn.setOnClickListener {
            val str = input.text.toString().filter { it.isDigit() }
            if (str.isEmpty()) { toast("Tutar giriniz"); return@setOnClickListener }
            val amount = str.toLong()
            if (amount <= 0) { toast("Tutar 0'dan büyük olmalı"); return@setOnClickListener }

            val list = prefs.load().toMutableList()
            val w = list.first { it.id == wish.id }
            if (isIncome) {
                if (w.current + amount > w.goal) {
                    AlertDialog.Builder(this)
                        .setMessage("Tutar hedefi aşıyor. Hedefe kadar kısılsın mı?")
                        .setPositiveButton("Evet") { _, _ ->
                            val delta = w.goal - w.current
                            if (delta > 0) {
                                w.current = w.goal
                                w.ops.add(Op(OpType.DEPOSIT, delta, System.currentTimeMillis()))
                            }
                            prefs.save(list)
                            finish()
                        }
                        .setNegativeButton("İptal", null).show()
                    return@setOnClickListener
                }
                w.current += amount
                w.ops.add(Op(OpType.DEPOSIT, amount, System.currentTimeMillis()))
            } else {
                if (w.current - amount < 0) {
                    AlertDialog.Builder(this).setMessage("Eksiye inilemez").setPositiveButton("Tamam", null).show()
                    return@setOnClickListener
                }
                w.current -= amount
                w.ops.add(Op(OpType.WITHDRAW, amount, System.currentTimeMillis()))
            }
            prefs.save(list)
            finish()
        }

        findViewById<android.widget.ImageView>(R.id.backIcon).setOnClickListener { finish() }
        // Ініціалізація AdMob
        MobileAds.initialize(this)
        // Показ банера
        val adView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        onBackPressedDispatcher.addCallback(this) { }
    }

    private fun toast(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
}