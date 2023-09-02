package com.bachhoangxuan.android.politicalpreparedness.database

import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionRepo(
    private val dao: ElectionDao, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insert(election: Election) {
        withContext(ioDispatcher) {
            dao.insert(election)
        }
    }

    suspend fun getById(id: String): BaseResult<Election> = withContext(ioDispatcher) {
        try {
            val election = dao.getById(id)
            return@withContext BaseResult.Success(election)
        } catch (e: Exception) {
            return@withContext BaseResult.Error(e.localizedMessage)
        }
    }

    suspend fun getAll(): BaseResult<List<Election>> = withContext(ioDispatcher) {
        return@withContext try {
            BaseResult.Success(dao.getAll())
        } catch (ex: Exception) {
            BaseResult.Error(ex.localizedMessage)
        }
    }

    suspend fun deleteById(id: String) {
        withContext(ioDispatcher) {
            dao.deleteById(id)
        }
    }

    suspend fun deleteAll() {
        withContext(ioDispatcher) {
            dao.deleteAll()
        }
    }
}