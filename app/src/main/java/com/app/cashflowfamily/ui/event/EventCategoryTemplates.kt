package com.app.cashflowfamily.ui.event

import com.app.cashflowfamily.data.model.EventCategoryIcon
import com.app.cashflowfamily.data.model.EventType

data class CategoryTemplate(
    val name: String,
    val iconKey: String,
    val colorHex: String
)

object EventCategoryTemplates {

    fun getTemplates(eventType: String): List<CategoryTemplate> {
        return when (eventType) {
            EventType.WEDDING.name -> weddingTemplates
            EventType.BIRTHDAY.name -> birthdayTemplates
            EventType.CORPORATE.name -> corporateTemplates
            EventType.RENOVATION.name -> renovationTemplates
            EventType.TRAVEL.name -> travelTemplates
            else -> defaultTemplates
        }
    }

    private val weddingTemplates = listOf(
        CategoryTemplate("Gedung / Venue", EventCategoryIcon.VENUE.name, "#2196F3"),
        CategoryTemplate("Catering", EventCategoryIcon.CATERING.name, "#FF9800"),
        CategoryTemplate("Dekorasi", EventCategoryIcon.DECORATION.name, "#E91E63"),
        CategoryTemplate("Foto & Video", EventCategoryIcon.PHOTO_VIDEO.name, "#9C27B0"),
        CategoryTemplate("Undangan", EventCategoryIcon.INVITATION.name, "#4CAF50"),
        CategoryTemplate("Busana & MUA", EventCategoryIcon.ATTIRE.name, "#FF5722"),
        CategoryTemplate("Entertainment / MC", EventCategoryIcon.ENTERTAINMENT.name, "#3F51B5"),
        CategoryTemplate("Souvenir", EventCategoryIcon.SOUVENIR.name, "#009688"),
        CategoryTemplate("Transportasi", EventCategoryIcon.TRANSPORT.name, "#607D8B"),
        CategoryTemplate("Cincin & Mas Kawin", EventCategoryIcon.RING.name, "#FFC107"),
        CategoryTemplate("Honeymoon", EventCategoryIcon.HONEYMOON.name, "#00BCD4"),
        CategoryTemplate("Lain-lain", EventCategoryIcon.OTHER.name, "#795548")
    )

    private val birthdayTemplates = listOf(
        CategoryTemplate("Venue", EventCategoryIcon.VENUE.name, "#2196F3"),
        CategoryTemplate("Kue & Snack", EventCategoryIcon.CAKE.name, "#FF9800"),
        CategoryTemplate("Dekorasi", EventCategoryIcon.DECORATION.name, "#E91E63"),
        CategoryTemplate("Entertainment", EventCategoryIcon.ENTERTAINMENT.name, "#9C27B0"),
        CategoryTemplate("Goodie Bag", EventCategoryIcon.SOUVENIR.name, "#4CAF50"),
        CategoryTemplate("Foto & Video", EventCategoryIcon.PHOTO_VIDEO.name, "#3F51B5"),
        CategoryTemplate("Lain-lain", EventCategoryIcon.OTHER.name, "#795548")
    )

    private val corporateTemplates = listOf(
        CategoryTemplate("Venue & Sewa", EventCategoryIcon.VENUE.name, "#2196F3"),
        CategoryTemplate("Konsumsi", EventCategoryIcon.CATERING.name, "#FF9800"),
        CategoryTemplate("Dekorasi & Backdrop", EventCategoryIcon.DECORATION.name, "#E91E63"),
        CategoryTemplate("Foto & Video", EventCategoryIcon.PHOTO_VIDEO.name, "#9C27B0"),
        CategoryTemplate("Transport & Akomodasi", EventCategoryIcon.TRANSPORT.name, "#607D8B"),
        CategoryTemplate("Merchandise", EventCategoryIcon.SHOPPING.name, "#3F51B5"),
        CategoryTemplate("Lain-lain", EventCategoryIcon.OTHER.name, "#795548")
    )

    private val renovationTemplates = listOf(
        CategoryTemplate("Material Bangunan", EventCategoryIcon.BUILDING.name, "#FF5722"),
        CategoryTemplate("Jasa Tukang", EventCategoryIcon.WORKER.name, "#2196F3"),
        CategoryTemplate("Furniture", EventCategoryIcon.FURNITURE.name, "#795548"),
        CategoryTemplate("Lain-lain", EventCategoryIcon.OTHER.name, "#607D8B")
    )

    private val travelTemplates = listOf(
        CategoryTemplate("Transportasi", EventCategoryIcon.TRANSPORT.name, "#2196F3"),
        CategoryTemplate("Akomodasi", EventCategoryIcon.ACCOMMODATION.name, "#FF9800"),
        CategoryTemplate("Makan & Minum", EventCategoryIcon.CATERING.name, "#E91E63"),
        CategoryTemplate("Tiket Wisata", EventCategoryIcon.TICKET.name, "#9C27B0"),
        CategoryTemplate("Belanja / Oleh-oleh", EventCategoryIcon.SHOPPING.name, "#4CAF50"),
        CategoryTemplate("Lain-lain", EventCategoryIcon.OTHER.name, "#795548")
    )

    private val defaultTemplates = listOf(
        CategoryTemplate("Kategori 1", EventCategoryIcon.OTHER.name, "#2196F3"),
        CategoryTemplate("Lain-lain", EventCategoryIcon.OTHER.name, "#795548")
    )
}