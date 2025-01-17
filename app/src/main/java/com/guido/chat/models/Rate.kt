package com.guido.chat.models

import java.util.*

data class Rate(
    val userId: String = "",
    val text: String = "",
    val rate: Float = 0f,
    val createdAt: Date = Date(),
    val profileImgURL: String = ""
)