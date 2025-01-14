package com.club.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.devfalah.repositories.CoreDataStoreDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CoreDataStoreDataSourceImp @Inject constructor(
    private val userDataStore: DataStore<Preferences>,
) : CoreDataStoreDataSource {

    override fun getUserId(): String? {
        return runBlocking {
            userDataStore.data.map {
                it[stringPreferencesKey(SIGN_UP_STATE_KEY)]
            }.first()
        }
    }

    override suspend fun saveUserId(userId: Int) {
        userDataStore.edit { preferences ->
            preferences[stringPreferencesKey(SIGN_UP_STATE_KEY)] = userId.toString()
        }
    }

    override suspend fun deleteUserId() {
        userDataStore.edit { preferences ->
            preferences[stringPreferencesKey(SIGN_UP_STATE_KEY)] = "-1"
        }

    }

    override fun getLanguage(): String? {
        return runBlocking {
            userDataStore.data.map {
                it[stringPreferencesKey(LANGUAGE_KEY)]
            }.first()
        }
    }

    override suspend fun saveLanguage(language: String) {
        userDataStore.edit { preferences ->
            preferences[stringPreferencesKey(LANGUAGE_KEY)] = language
        }
    }

    companion object {
        const val SIGN_UP_STATE_KEY = "sign_up_state_key"
        const val LANGUAGE_KEY = "language_key"
    }
}