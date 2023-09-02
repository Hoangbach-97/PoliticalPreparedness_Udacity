package com.bachhoangxuan.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bachhoangxuan.android.politicalpreparedness.databinding.ElectionItemBinding
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election

class ElectionListAdapter(private val clickElectionListener: ElectionListener) :
    ListAdapter<Election, ElectionListAdapter.ElectionViewHolder>(ElectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder.from(parent)
    }

    // TODO: Bind ViewHolder
    override fun onBindViewHolder(electionViewHolder: ElectionViewHolder, position: Int) {
        val electionItem = getItem(position)
        electionViewHolder.bind(clickElectionListener, electionItem)
    }

    class ElectionViewHolder private constructor(private val binding: ElectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: ElectionListener, item: Election) {
            binding.executePendingBindings()
            binding.electionModel = item
            binding.clickElectionListener = clickListener
        }

        // TODO: Add companion object to inflate ViewHolder (from)
        companion object {
            fun from(parent: ViewGroup): ElectionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElectionItemBinding.inflate(layoutInflater, parent, false)
                return ElectionViewHolder(binding)
            }
        }
    }
}


// TODO: Create ElectionDiffCallback
class ElectionDiffCallback : DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.id == newItem.id
    }
}

// TODO: Create ElectionListener
class ElectionListener(val clickListener: (election: Election) -> Unit) {
    fun onClick(election: Election) = clickListener(election)
}