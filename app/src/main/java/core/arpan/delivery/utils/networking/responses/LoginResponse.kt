package core.arpan.delivery.utils.networking.responses

import core.arpan.delivery.models.Tokens
import core.arpan.delivery.models.User

data class LoginResponse(
    val error: Boolean?,
    val message: String?,
    val tokens: Tokens?,
    val user: User?
)