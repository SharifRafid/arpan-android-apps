package core.arpan.delivery.utils.networking.responses

import core.arpan.delivery.models.Access
import core.arpan.delivery.models.Refresh

data class RefreshResponse(
    val error: Boolean?,
    val message: String?,
    val access: Access?,
    val refresh: Refresh?
)