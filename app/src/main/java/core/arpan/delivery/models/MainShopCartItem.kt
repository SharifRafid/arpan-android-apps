package core.arpan.delivery.models

data class MainShopCartItem(
    var key : String = "",
    var shop_doc_id : String = "",
    var cart_products : ArrayList<CartProductEntity> = ArrayList(),
    var shop_details : Shop = Shop()
)