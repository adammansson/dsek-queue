package api

import play.api.MarkerContext
import play.api.libs.json._

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

/**
  * DTO for displaying order information.
  */
case class OrderResource(id: Int, content: String, timePlaced: Long, isDone: Boolean)

object OrderResource {
  /**
    * Mapping to read/write a api.OrderResource out as a JSON value.
    */
    implicit val format: Format[OrderResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[OrderResource]]
  */
class OrderResourceHandler @Inject()(
                                     routerProvider: Provider[QueueRouter],
                                     orderRepository: OrderRepository)(implicit ec: ExecutionContext) {

  def create(orderInput: OrderFormInput)(
      implicit mc: MarkerContext): Future[OrderResource] = {
    val data = OrderData(OrderId(), orderInput.content, Instant.now().getEpochSecond(), false)
    // We don't actually create the order here, so return what we have
    orderRepository.create(data).map { id =>
      createOrderResource(data)
    }
  }

  def markDone(id: String)(
    implicit mc: MarkerContext): Future[Option[OrderResource]] = {
    orderRepository.markDone(OrderId(id))
    lookup(id)
  }

  def delete(id: String)(
    implicit mc: MarkerContext): Future[Option[OrderResource]] = {
    val temp = lookup(id)
    orderRepository.delete(OrderId(id))
    temp

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
    OrderResource(p.id.underlying, p.content, p.timePlaced, p.isDone)
  }
}
