package app.myoun.comcigan

import kotlinx.serialization.Serializable

@Serializable
data class Period(
    val subject: String,
    val teacher: String
)