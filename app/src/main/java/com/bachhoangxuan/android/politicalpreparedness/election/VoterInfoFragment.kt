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
        // TODO: Add ViewModel values and create ViewModel
        var electionDatabase: Election? = null
        var votingUrl = ""
        var ballotUrl = ""
        val fragmentVoterInfoBinding: FragmentVoterInfoBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_voter_info, container, false,
        )

        // TODO: Add binding values
        fragmentVoterInfoBinding.voterInfoViewModel = voterInfoViewModel
        fragmentVoterInfoBinding.lifecycleOwner = this
        val argDivision = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision
        val argElectionId = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId
        voterInfoViewModel.getElectionById(argElectionId)
        voterInfoViewModel.getPopulateVoterInfo(argElectionId, argDivision.id)

        // TODO: Populate voter info -- hide views without provided data.
        voterInfoViewModel.electionLiveData.observe(viewLifecycleOwner) { electionData ->
            electionDatabase = electionData
        }

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
         */

        // TODO: Handle loading of URLs
        voterInfoViewModel.voterInfoResponseLiveData.observe(viewLifecycleOwner) { voterInfo ->
            voterInfo.let {
                if (!voterInfo.state.isNullOrEmpty()) {
                    votingUrl =
                        voterInfo.state.first().electionAdministrationBody.votingLocationFinderUrl
                            ?: ""
                    ballotUrl =
                        voterInfo.state.first().electionAdministrationBody.ballotInfoUrl ?: ""
                } else {
                    fragmentVoterInfoBinding.addressGroup.visibility = View.GONE
                }

            }
        }

        // TODO: Handle save button UI state
        fragmentVoterInfoBinding.stateLocations.setOnClickListener {
            if (votingUrl.isNotEmpty()) {
                loadURLIntents(votingUrl)
            } else {
                null
            }
        }
        fragmentVoterInfoBinding.stateBallot.setOnClickListener {
            if (ballotUrl.isNotEmpty()) {
                loadURLIntents(ballotUrl)
            } else {
                null
            }
        }
        // TODO: cont'd Handle save button clicks
        fragmentVoterInfoBinding.saveElectionButton.setOnClickListener {
            if (electionDatabase != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    voterInfoViewModel.removeElectionById(argElectionId)
                    Navigation.findNavController(requireView()).popBackStack()
                }

            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    voterInfoViewModel.saveElectionToDatabase()
                    Navigation.findNavController(requireView()).popBackStack()
                }
            }
        }

        return fragmentVoterInfoBinding.root
    }

    // TODO: Create method to load URL intents
    private fun loadURLIntents(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}