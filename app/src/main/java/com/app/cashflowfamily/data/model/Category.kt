package com.app.cashflowfamily.data.model

object Categories {

    val INCOME_CATEGORIES = listOf(
        "Gaji",
        "Bonus",
        "Bisnis",
        "Investasi",
        "Hadiah",
        "Freelance",
        "Lainnya"
    )

    val EXPENSE_CATEGORIES = listOf(
        "Makanan & Minuman",
        "Transportasi",
        "Belanja",
        "Tagihan",
        "Kesehatan",
        "Pendidikan",
        "Hiburan",
        "Rumah Tangga",
        "Anak",
        "Sosial",
        "Lainnya"
    )

    fun getCategories(type: String): List<String> {
        return if (type == "income") INCOME_CATEGORIES else EXPENSE_CATEGORIES
    }
}