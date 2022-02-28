package api

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

/**
  * Routes and URLs to the api.QueueResource controller.
  */
class QueueRouter @Inject()(controller: QueueController) extends SimpleRouter {
  val prefix = "/api"

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.processCreate

    case GET(p"/$id") =>
      controller.show(id)

    case POST(p"/$id/update") =>
      controller.processUpdate(id)

    case PUT(p"/$id/done") =>
      controller.markDone(id)

    case DELETE(p"/$id") =>
      controller.delete(id)
  }
}
