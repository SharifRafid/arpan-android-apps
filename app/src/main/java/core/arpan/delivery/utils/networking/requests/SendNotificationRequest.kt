package core.arpan.delivery.utils.networking.requests

class SendNotificationRequest(
  var userId : String? = null,
  var orderId : String? = null,
  var daId : String? = null,
  var title : String? = null,
  var body : String? = null,
  var dialogTitle : String? = null,
  var dialogBody : String? = null,
  var clickAction : String? = null,
)