package admin.arpan.delivery.utils.networking.responses

import core.arpan.delivery.models.Category

data class GetAllCategoriesResponse(
    var error: Boolean? = null,
    var message: String? = null,
    val limit: Int? = null,
    val page: Int? = null,
    val results: List<Category>? = null,
    val totalPages: Int? = null,
    val totalResults: Int? = null
)
