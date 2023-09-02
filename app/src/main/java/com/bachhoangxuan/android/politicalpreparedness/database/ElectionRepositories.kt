package com.bachhoangxuan.android.politicalpreparedness.database

import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionRepositories(
    private val dao: ElectionDao, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insertElection(election: Election) {
        withContext(ioDispatcher) {
            dao.insertElection(election)
        }
    }

    suspend fun getElections(): BaseResult<List<Election>> = withContext(ioDispatcher) {
        return@withContext try {
            BaseResult.Success(dao.getElections())
        } catch (ex: Exception) {
            BaseResult.Error(ex.localizedMessage)
        }
    }

    suspend fun getElectionItemById(id: String): BaseResult<Election> = withContext(ioDispatcher) {
        try {
            val election = dao.getElectionItemById(id)
            return@withContext BaseResult.Success(election)
        } catch (e: Exception) {
            return@withContext BaseResult.Error(e.localizedMessage)
        }
    }

    suspend fun deleteElectionItemById(id: String) {
        withContext(ioDispatcher) {
            dao.deleteElectionItemById(id)
        }
    }

    suspend fun clearElections() {
        withContext(ioDispatcher) {
            dao.clearElections()
        }
    }
}