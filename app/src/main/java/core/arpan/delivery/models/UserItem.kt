package core.arpan.delivery.models

data class UserItem(
    var key : String = "",
    var address : String = "",
    var name : String = "",
    var phone : String = "",
    var profile_image : String = "",
    var ordersCountLastMonth : Int = 0,
    var orderLastMonth : ArrayList<OrderItemMain> = ArrayList(),
    var ordersCountThisMonth : Int = 0,
    var orderThisMonth : ArrayList<OrderItemMain> = ArrayList(),
)
