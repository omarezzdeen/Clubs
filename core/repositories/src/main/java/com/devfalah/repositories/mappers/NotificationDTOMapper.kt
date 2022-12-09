package com.devfalah.repositories.mappers

import com.devfalah.entities.Notification
import com.devfalah.repositories.ConvertDate
import com.devfalah.repositories.NotificationType
import com.devfalah.repositories.models.notification.NotificationsDTO

fun NotificationsDTO.toEntity(): Notification {
    val notificationType = when (this.notification?.type) {
        "like:entity:file:ossn:aphoto" -> NotificationType.LIKE_PHOTO
        "like:post" -> NotificationType.LIKE_POST
        "group:joinrequest" -> NotificationType.REQUEST_GROUP
        else -> null
    }

    return Notification(
        entity = this.entity ?: false,
        post = this.post ?: false,
        guid = this.notification?.guid ?: 0,
        itemGuid = this.notification?.itemGuid ?: 0,
        ownerGuid = this.notification?.ownerGuid ?: 0,
        subjectGuid = this.notification?.subjectGuid ?: 0,
        timeCreated = this.notification?.timeCreated?.let { ConvertDate().convertTime(it) } ?: "",
        type = notificationType?.value ?: -1,
        viewed = this.notification?.viewed != null,
        posterID = this.notification?.posterGuid ?: 0,
        posterName = poster?.fullname ?: "",
        posterImage = poster?.icon ?: ""
    )
}