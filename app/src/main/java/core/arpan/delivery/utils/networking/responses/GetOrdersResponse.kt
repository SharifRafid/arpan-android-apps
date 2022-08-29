package core.arpan.delivery.utils.networking.responses

import core.arpan.delivery.models.OrderItemMain

data class GetOrdersResponse(
    val error: Boolean?,
    val message: String?,
    val results: ArrayList<OrderItemMain>?,
    val page: Int?,
    val limit: Int?,
    val totalPages: Int?,
    val totalResults: Int?,
)