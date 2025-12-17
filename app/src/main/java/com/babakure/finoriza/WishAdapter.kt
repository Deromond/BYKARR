package com.babakure.finoriza

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class WishAdapter(
    private val onDeposit: (Wish) -> Unit,
    private val onEdit: (Wish) -> Unit,
    private val onArchive: (Wish) -> Unit,
    private val onPurchased: (Wish) -> Unit,
    private val onDelete: (Wish) -> Unit
) : RecyclerView.Adapter<WishAdapter.VH>() {

    private val items = mutableListOf<Wish>()

    fun submit(list: List<Wish>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_wish, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(
            items[position],
            onDeposit = onDeposit,
            onEdit = onEdit,
            onArchive = onArchive,
            onPurchased = onPurchased,
            onDelete = onDelete
        )
    }

    override fun getItemCount() = items.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {

        private val title: TextView = v.findViewById(R.id.title)
        private val date: TextView = v.findViewById(R.id.date)
        private val sum: TextView = v.findViewById(R.id.sum)
        private val progress: ProgressBar = v.findViewById(R.id.progress)

        private val btnControl: View = v.findViewById(R.id.btnControl)
        private val panel: View = v.findViewById(R.id.actionPanel)
        private val headerMoney: View = v.findViewById(R.id.headerMoney)

        private val btnArchive: View = v.findViewById(R.id.btnArchive)
        private val btnEdit: View = v.findViewById(R.id.btnEdit)
        private val btnDone: View = v.findViewById(R.id.btnDone)

        // Додано:
        private val btnDeleteArchived: View = v.findViewById(R.id.btnDeleteArchived) // кругла в шапці (архів/куплено)
        private val btnDeleteActive: View? = v.findViewById(R.id.btnDeleteActive)    // delete у панелі дій (active)

        fun bind(
            w: Wish,
            onDeposit: (Wish) -> Unit,
            onEdit: (Wish) -> Unit,
            onArchive: (Wish) -> Unit,
            onPurchased: (Wish) -> Unit,
            onDelete: (Wish) -> Unit
        ) {
            title.text = w.title
            date.text = android.text.format.DateFormat.format("dd.MM.yyyy", w.createdAt)
            sum.text = "${formatMoney(w.current)} ₺ | ${formatMoney(w.goal)} ₺"
            val percent = (w.current.toDouble() / w.goal.toDouble() * 100).coerceIn(0.0, 100.0)
            animateProgress(progress, percent.roundToInt())

            // Контроль UI за статусом
            if (w.status == WishStatus.ACTIVE) {
                // На активних: панель доступна (показ за toggle), кругла архівна-кнопка delete в шапці схована
                btnControl.visibility = View.VISIBLE
                panel.visibility = View.GONE
                btnControl.isSelected = false
                headerMoney.visibility = View.VISIBLE
                btnDeleteArchived.visibility = View.GONE
            } else {
                // На ARCHIVED/PURCHASED: тільки кругла кнопка delete у шапці
                btnControl.visibility = View.GONE
                panel.visibility = View.GONE
                btnControl.isSelected = false
                headerMoney.visibility = View.GONE
                btnDeleteArchived.visibility = View.VISIBLE
            }

            // Тогл панелі дій (для ACTIVE)
            btnControl.setOnClickListener {
                val nowVisible = panel.visibility != View.VISIBLE
                panel.visibility = if (nowVisible) View.VISIBLE else View.GONE
                btnControl.isSelected = nowVisible
            }

            // Дії панелі (ACTIVE)
            btnArchive.setOnClickListener { onDelete(w) } // remapped: this is actually DELETE
            btnEdit.setOnClickListener { onEdit(w) }
            btnDone.setOnClickListener { onArchive(w) } // remapped: this is actually ARCHIVE
            btnDeleteActive?.setOnClickListener { onDelete(w) } // видалення для активних

            // Поповнення з шапки
            headerMoney.setOnClickListener { onDeposit(w) }

            // Видалення для архівованих/куплених: лише виклик onDelete, підтвердження робить Activity
            btnDeleteArchived.setOnClickListener { onDelete(w) }
        }

        private fun animateProgress(pb: ProgressBar, to: Int) {
            val from = pb.progress
            val anim = ValueAnimator.ofInt(from, to)
            anim.duration = 350
            anim.addUpdateListener { pb.progress = it.animatedValue as Int }
            anim.start()
        }

        private fun formatMoney(v: Long): String {
            val s = v.toString()
            val b = StringBuilder()
            var c = 0
            for (i in s.length - 1 downTo 0) {
                b.append(s[i])
                c++
                if (c == 3 && i > 0) {
                    b.append(' ')
                    c = 0
                }
            }
            return b.reverse().toString()
        }
    }
}
