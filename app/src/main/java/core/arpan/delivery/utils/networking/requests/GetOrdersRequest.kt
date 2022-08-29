package core.arpan.delivery.utils.networking.requests

import core.arpan.delivery.models.enums.OrderStatus

class GetOrdersRequest(
    var startTimeMillis: Long? = null,
    var endTimeMillis: Long? = null,
    var limit: Int? = null,
    var page: Int? = null,
    var daID: String? = null,
    var orderStatus: OrderStatus? = null,
)