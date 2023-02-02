package hr.algebra.apirequests.model

data class Joke (
    val createdAt : String,
    val iconURL   : String,
    val id        : String,
    val updatedAt : String,
    val url       : String,
    val value     : String
)