package com.devfalah.usecases

import com.devfalah.entities.Club
import com.devfalah.usecases.repository.ClubRepository
import javax.inject.Inject

class JoinClubUseCase @Inject constructor(
    private val clubRepository: ClubRepository
) {
    suspend operator fun invoke(clubId: Int, userId: Int): Club {
        return clubRepository.joinClub(clubId, userId)
    }
}