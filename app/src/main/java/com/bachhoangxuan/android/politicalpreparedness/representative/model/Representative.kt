package com.bachhoangxuan.android.politicalpreparedness.representative.model

import com.bachhoangxuan.android.politicalpreparedness.network.models.Office
import com.bachhoangxuan.android.politicalpreparedness.network.models.Official

data class Representative (
        val official: Official,
        val office: Office
)