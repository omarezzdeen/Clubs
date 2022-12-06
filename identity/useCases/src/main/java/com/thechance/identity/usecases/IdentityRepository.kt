package com.thechance.identity.usecases

import com.thechance.identity.entities.Account

interface IdentityRepository {

    suspend fun login(userName: String, password: String): Boolean //User

    suspend fun signup(
        firstname: String, lastname: String, email: String, reEmail: String,
        gender: String, birthdate: String, username: String, password: String
    ): Account
}