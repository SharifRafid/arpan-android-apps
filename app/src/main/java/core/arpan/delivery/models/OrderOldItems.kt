package core.arpan.delivery.models

data class OrderOldItems(
    var key : String = "",
    var date : String = "",
    var orders : ArrayList<OrderItemMain> = ArrayList()
)