package com.babakure.finoriza

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class WishStatus { ACTIVE, ARCHIVED, PURCHASED }
enum class OpType { DEPOSIT, WITHDRAW, EDIT, ARCHIVE, PURCHASE }

data class Op(val type: OpType, val amount: Long?, val at: Long)

data class Wish(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var goal: Long,
    var current: Long = 0L,
    var createdAt: Long = System.currentTimeMillis(),
    var status: WishStatus = WishStatus.ACTIVE,
    val ops: MutableList<Op> = mutableListOf()
)

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("bykar_fin_prefs", Context.MODE_PRIVATE)

    fun load(): MutableList<Wish> {
        val raw = sp.getString("wishes", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Wish>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val w = Wish(
                id = o.getString("id"),
                title = o.getString("title"),
                goal = o.getLong("goal"),
                current = o.optLong("current", 0L),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                status = WishStatus.valueOf(o.optString("status","ACTIVE"))
            )
            val opsArr = o.optJSONArray("ops") ?: JSONArray()
            for (j in 0 until opsArr.length()) {
                val x = opsArr.getJSONObject(j)
                w.ops.add(Op(OpType.valueOf(x.getString("type")), if (x.has("amount")) x.getLong("amount") else null, x.getLong("at")))
            }
            list.add(w)
        }
        return list
    }

    fun save(list: List<Wish>) {
        val arr = JSONArray()
        for (w in list) {
            val o = JSONObject()
            o.put("id", w.id)
            o.put("title", w.title)
            o.put("goal", w.goal)
            o.put("current", w.current)
            o.put("createdAt", w.createdAt)
            o.put("status", w.status.name)
            val opsArr = JSONArray()
            for (op in w.ops) {
                val xo = JSONObject()
                xo.put("type", op.type.name)
                if (op.amount != null) xo.put("amount", op.amount)
                xo.put("at", op.at)
                opsArr.put(xo)
            }
            o.put("ops", opsArr)
            arr.put(o)
        }
        sp.edit().putString("wishes", arr.toString()).apply()
    }
}