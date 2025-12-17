package com.babakure.finoriza

import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class WishFormActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersiveFullscreen()
        setContentView(R.layout.activity_wish_form)
        prefs = Prefs(this)

        val name = findViewById<EditText>(R.id.inputName)
        val cost = findViewById<EditText>(R.id.inputCost)
        val btn = findViewById<Button>(R.id.btnSave)

        name.filters = arrayOf(InputFilter.LengthFilter(9))
        cost.filters = arrayOf(InputFilter.LengthFilter(6))

        // digits only
        cost.setOnKeyListener { _, _, _ -> false }
        cost.setOnFocusChangeListener { _, _ ->
            val t = cost.text.toString()
            cost.setText(t.filter { it.isDigit() }.take(6))
        }

        editingId = intent.getStringExtra("wishId")
        if (editingId != null) {
            title = "Düzenle"
            val w = prefs.load().first { it.id == editingId }
            name.setText(w.title)
            cost.setText(w.goal.toString())
            btn.text = "Kaydet"
        }

        btn.setOnClickListener {
            val titleStr = name.text.toString().trim()
            val costStr = cost.text.toString().trim()
            if (titleStr.isEmpty()) { toast("Ad giriniz"); return@setOnClickListener }
            if (costStr.isEmpty()) { toast("Tutar giriniz"); return@setOnClickListener }
            val goal = costStr.toLong()
            if (goal <= 0L) { toast("Tutar 0'dan büyük olmalı"); return@setOnClickListener }

            val list = prefs.load().toMutableList()
            if (editingId == null) {
                list.add(Wish(title = titleStr, goal = goal))
            } else {
                val w = list.first { it.id == editingId }
                if (goal < w.current) {
                    AlertDialog.Builder(this).setMessage("Yeni hedef mevcut tutardan küçük").setPositiveButton("Tamam", null).show()
                    return@setOnClickListener
                }
                w.title = titleStr
                w.goal = goal
                w.ops.add(Op(OpType.EDIT, null, System.currentTimeMillis()))
            }
            prefs.save(list)
            finish()
        }
        onBackPressedDispatcher.addCallback(this) { }
        findViewById<android.widget.ImageView>(R.id.backIcon).setOnClickListener { finish() }
    }

    private fun toast(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
}