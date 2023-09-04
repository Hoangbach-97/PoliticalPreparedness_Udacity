package com.bachhoangxuan.android.politicalpreparedness.representative.model

import android.os.Parcelable
import com.bachhoangxuan.android.politicalpreparedness.network.models.Office
import com.bachhoangxuan.android.politicalpreparedness.network.models.Official
import kotlinx.parcelize.Parcelize

@Parcelize
data class Representative (
        val official: Official,
        val office: Office
): Parcelable