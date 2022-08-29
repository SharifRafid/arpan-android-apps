package core.arpan.delivery.models

import core.arpan.delivery.models.Access
import core.arpan.delivery.models.Refresh

data class Tokens(
    val access: Access,
    val refresh: Refresh
)