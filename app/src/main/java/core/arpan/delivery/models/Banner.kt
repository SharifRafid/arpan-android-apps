package core.arpan.delivery.models

data class Banner(
    var error: Boolean? = null,
    var message: String? = null,
    var icon: String? = null,
    var id: String? = null,
    var order: Int? = null
)