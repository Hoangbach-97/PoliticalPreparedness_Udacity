package com.bachhoangxuan.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.bachhoangxuan.android.politicalpreparedness.R
import com.bachhoangxuan.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import com.bachhoangxuan.android.politicalpreparedness.util.Constants
import kotlinx.coroutines.launch

@Suppress("UNUSED_EXPRESSION")
class VoterInfoFragment : Fragment() {
    private val voterInfoViewModel: VoterInfoViewModel by lazy {
        val application = requireNotNull(this.activity).application
        val viewModelFactory = VoterInfoViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        var election: Election? = null
        var votingUrl = ""
        var ballotUrl = ""
        val binding: FragmentVoterInfoBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_voter_info, container, false,
        )

        binding.voterInfoViewModel = voterInfoViewModel
        binding.lifecycleOwner = this
        val argDivision = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision
        val argElectionId = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId
        voterInfoViewModel.getElection(argElectionId)
        voterInfoViewModel.getVoterInfo(argElectionId, argDivision.id)

        voterInfoViewModel.electionLiveData.observe(viewLifecycleOwner) { electionData ->
            election = electionData
        }

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
         */

        voterInfoViewModel.voterInfoResponse.observe(viewLifecycleOwner) { voterInfo ->
            voterInfo.let {
                if (!voterInfo.state.isNullOrEmpty()) {
                    votingUrl =
                        voterInfo.state.first().electionAdministrationBody.votingLocationFinderUrl
                            ?: Constants.EMPTY_STRING
                    ballotUrl =
                        voterInfo.state.first().electionAdministrationBody.ballotInfoUrl
                            ?: Constants.EMPTY_STRING
                } else {
                    binding.addressGroup.visibility = View.GONE
                }

            }
        }

        binding.stateLocations.setOnClickListener {
            if (votingUrl.isNotEmpty()) {
                loadUrl(votingUrl)
            } else {
                null
            }
        }
        binding.stateBallot.setOnClickListener {
            if (ballotUrl.isNotEmpty()) {
                loadUrl(ballotUrl)
            } else {
                null
            }
        }
        binding.saveElectionButton.setOnClickListener {
            if (election != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    voterInfoViewModel.deleteElection(argElectionId)
                    Navigation.findNavController(requireView()).popBackStack()
                }

            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    voterInfoViewModel.saveElection()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        }

        return binding.root
    }

    private fun loadUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}