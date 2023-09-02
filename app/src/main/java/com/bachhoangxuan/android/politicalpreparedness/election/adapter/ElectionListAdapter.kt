package com.bachhoangxuan.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bachhoangxuan.android.politicalpreparedness.databinding.ElectionItemBinding
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election

class ElectionListAdapter(private val clickElectionListener: ElectionListener) :
    ListAdapter<Election, ElectionListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val electionItem = getItem(position)
        viewHolder.bind(clickElectionListener, electionItem)
    }

    class ViewHolder private constructor(private val binding: ElectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: ElectionListener, item: Election) {
            binding.executePendingBindings()
            binding.electionModel = item
            binding.clickElectionListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElectionItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class ElectionListener(val clickListener: (election: Election) -> Unit) {
    fun onClick(election: Election) = clickListener(election)
}

class DiffCallback : DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.id == newItem.id
    }
}