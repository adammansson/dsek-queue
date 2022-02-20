package api

import play.api.MarkerContext
import play.api.libs.json._

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying order information.
  */
case class OrderResource(id: String, link: String, title: String, body: String)

object OrderResource {
  /**
    * Mapping to read/write a api.OrderResource out as a JSON value.
    */
    implicit val format: Format[OrderResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[QueueResource]]
  */
class OrderResourceHandler @Inject()(
                                     routerProvider: Provider[QueueRouter],
                                     orderRepository: OrderRepository)(implicit ec: ExecutionContext) {

  def create(orderInput: OrderFormInput)(
      implicit mc: MarkerContext): Future[OrderResource] = {
    val data = OrderData(OrderId("999"), orderInput.title, orderInput.body)
    // We don't actually create the Queue, so return what we have
    orderRepository.create(data).map { id =>
      createOrderResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[OrderResource]] = {
    val orderFuture = orderRepository.get(OrderId(id))
    orderFuture.map { maybeOrderData =>
      maybeOrderData.map { orderData =>
        createOrderResource(orderData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[OrderResource]] = {
    orderRepository.list().map { orderDataList =>
      orderDataList.map(orderData => createOrderResource(orderData))
    }
  }

  private def createOrderResource(p: OrderData): OrderResource = {
    OrderResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}
