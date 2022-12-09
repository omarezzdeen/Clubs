package com.devfalah.repositories.mappers

import com.devfalah.entities.User
import com.devfalah.repositories.models.FriendDTO


fun FriendDTO.toEntity(): User {
    return User(
        birthdate = this.birthdate ?: "",
        coverUrl = this.coverUrl ?: "",
        email = this.email ?: "",
        name = this.firstName ?: "",
        title = this.lastName?.substringBefore("$%") ?: "",
        bio = this.lastName?.substringAfter("$%") ?: "",
        username = this.username ?: "",
        gender = this.gender ?: "",
        id = this.guid ?: 0,
        language = this.language ?: "",
        profileUrl = this.icon?.large ?: "",
        isFriend = false
    )
}
