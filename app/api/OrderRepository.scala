package api

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

final case class OrderData(id: OrderId, title: String, body: String)

class OrderId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object OrderId {
  def apply(raw: String): OrderId = {
    require(raw != null)
    new OrderId(Integer.parseInt(raw))
  }
}

class QueueExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the api.QueueRepository.
  */
trait OrderRepository {
  def create(data: OrderData)(implicit mc: MarkerContext): Future[OrderId]

  def list()(implicit mc: MarkerContext): Future[Iterable[OrderData]]

  def get(id: OrderId)(implicit mc: MarkerContext): Future[Option[OrderData]]
}

/**
  * A trivial implementation for the Queue Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class OrderRepositoryImpl @Inject()()(implicit ec: QueueExecutionContext)
    extends OrderRepository {

  private val logger = Logger(this.getClass)

  private val OrderList = List(
    OrderData(OrderId("0"), "order 0", "dboll"),
    OrderData(OrderId("1"), "order 1", "macka"),
    OrderData(OrderId("2"), "order 2", "pölse"),
    OrderData(OrderId("3"), "order 3", "")
  )

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[OrderData]] = {
    Future {
      logger.trace(s"list: ")
      OrderList
    }
  }

  override def get(id: OrderId)(
      implicit mc: MarkerContext): Future[Option[OrderData]] = {
    Future {
      logger.trace(s"get: id = $id")
      OrderList.find(order => order.id == id)
    }
  }

  def create(data: OrderData)(implicit mc: MarkerContext): Future[OrderId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
