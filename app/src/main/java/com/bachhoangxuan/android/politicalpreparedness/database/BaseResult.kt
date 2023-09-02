package com.bachhoangxuan.android.politicalpreparedness.database

sealed class BaseResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : BaseResult<T>()
    data class Error(val message: String?, val statusCode: Int? = null) : BaseResult<Nothing>()
}