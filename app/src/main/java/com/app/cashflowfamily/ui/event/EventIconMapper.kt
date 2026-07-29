package com.app.cashflowfamily.ui.event

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Chair
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.cashflowfamily.data.model.EventCategoryIcon
import com.app.cashflowfamily.data.model.EventType

fun String.toEventCategoryIcon(): ImageVector {
    return when (this) {
        EventCategoryIcon.VENUE.name -> Icons.Filled.LocationCity
        EventCategoryIcon.CATERING.name -> Icons.Filled.Restaurant
        EventCategoryIcon.DECORATION.name -> Icons.Filled.AutoAwesome
        EventCategoryIcon.PHOTO_VIDEO.name -> Icons.Filled.PhotoCamera
        EventCategoryIcon.INVITATION.name -> Icons.Filled.Mail
        EventCategoryIcon.ATTIRE.name -> Icons.Filled.Checkroom
        EventCategoryIcon.ENTERTAINMENT.name -> Icons.Filled.Mic
        EventCategoryIcon.SOUVENIR.name -> Icons.Filled.Redeem
        EventCategoryIcon.TRANSPORT.name -> Icons.Filled.DirectionsCar
        EventCategoryIcon.RING.name -> Icons.Filled.Diamond
        EventCategoryIcon.HONEYMOON.name -> Icons.Filled.FlightTakeoff
        EventCategoryIcon.CAKE.name -> Icons.Filled.Cake
        EventCategoryIcon.ACCOMMODATION.name -> Icons.Filled.Hotel
        EventCategoryIcon.TICKET.name -> Icons.Filled.ConfirmationNumber
        EventCategoryIcon.SHOPPING.name -> Icons.Filled.ShoppingBag
        EventCategoryIcon.BUILDING.name -> Icons.Filled.Foundation
        EventCategoryIcon.WORKER.name -> Icons.Filled.Engineering
        EventCategoryIcon.FURNITURE.name -> Icons.Filled.Chair
        else -> Icons.Filled.Category
    }
}

fun String.toEventTypeIcon(): ImageVector {
    return when (this) {
        EventType.WEDDING.name -> Icons.Filled.Diamond
        EventType.BIRTHDAY.name -> Icons.Filled.Cake
        EventType.CORPORATE.name -> Icons.Filled.LocationCity
        EventType.RENOVATION.name -> Icons.Filled.Foundation
        EventType.TRAVEL.name -> Icons.Filled.FlightTakeoff
        else -> Icons.Filled.Celebration
    }
}