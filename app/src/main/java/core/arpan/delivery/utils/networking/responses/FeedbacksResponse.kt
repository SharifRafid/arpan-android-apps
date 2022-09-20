package core.arpan.delivery.utils.networking.responses

import core.arpan.delivery.models.Feedback
import core.arpan.delivery.models.OrderItemMain

data class FeedbacksResponse(
    val error: Boolean? = null,
    val message: String? = null,
    val results: ArrayList<Feedback>? = ArrayList(),
    val page: Int? = null,
    val limit: Int? = null,
    val totalPages: Int? = null,
    val totalResults: Int? = null,
)