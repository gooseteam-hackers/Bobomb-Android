package org.gooseprjkt.bobomb.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import org.gooseprjkt.bobomb.BuildVars
import org.gooseprjkt.bobomb.databinding.AttackWorkRowBinding
import org.gooseprjkt.bobomb.ui.MainViewModel
import org.gooseprjkt.bobomb.worker.AttackWorker
import java.text.SimpleDateFormat
import java.util.*

class BobombWorkAdapter(
    private val context: Context,
    private val callback: (WorkInfo?) -> Unit
) :
    RecyclerView.Adapter<BobombWorkAdapter.ViewHolder>() {

    private var workInfo: List<WorkInfo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            AttackWorkRowBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workInfo = workInfo[position]

        val isRunning = workInfo.state == WorkInfo.State.RUNNING

        holder.binding.taskProgress.visibility = if (isRunning) View.VISIBLE else View.INVISIBLE
        holder.binding.taskTime.visibility = if (isRunning) View.GONE else View.VISIBLE

        if (isRunning) {
            holder.binding.taskProgress.setMax(workInfo.progress.getInt(MainViewModel.KEY_MAX_PROGRESS, 0))
            holder.binding.taskProgress.setProgress(workInfo.progress.getInt(MainViewModel.KEY_PROGRESS, 0))
        }

        val dateFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

        var isDripMode = false
        var phoneNumber = ""

        for (tag in workInfo.tags) {
            // Проверяем тег drip_mode
            if (tag == "drip_mode") {
                isDripMode = true
                continue
            }
            
            if (tag.startsWith(AttackWorker::class.java.getCanonicalName()!!) || tag == MainViewModel.ATTACK)
                continue

            val parts = tag.split(";")

            phoneNumber = parts[0]
            holder.binding.taskTitle.text = phoneNumber

            for (i in BuildVars.COUNTRY_CODES.indices) {
                if (parts[0].substring(1).startsWith(BuildVars.COUNTRY_CODES[i])) {
                    holder.binding.countryFlag.setImageResource(BuildVars.COUNTRY_FLAGS[i])
                    break
                }
            }

            if (parts.size == 2) {
                val scheduledTime = parts[1].toLong()
                val currentTime = System.currentTimeMillis()

                // Drip mode: scheduled more than 1 minute in the future
                isDripMode = scheduledTime > currentTime + 60000

                holder.binding.taskTime.text = dateFormat.format(Date(scheduledTime))
            }
        }

        // Show drip icon for drip mode attacks
        holder.binding.dripIcon.visibility = if (isDripMode) View.VISIBLE else View.GONE
    }

    fun setWorkInfo(workInfo: List<WorkInfo>) {
        this.workInfo = workInfo
    }

    override fun getItemCount(): Int = workInfo.size

    inner class ViewHolder(val binding: AttackWorkRowBinding) : RecyclerView.ViewHolder(
        binding.getRoot()
    ), View.OnClickListener {
        init {
            binding.stopAttack.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            callback(workInfo[layoutPosition])
        }
    }
}
