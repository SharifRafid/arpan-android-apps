package core.arpan.delivery.models

data class CommonResponse<T>(
    val data: T?,
    val error: Boolean?
)