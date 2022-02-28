package api

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import java.time.Instant
import scala.collection.mutable.ListBuffer

/**
 * The class representing an order.
 */
final case class OrderData(id: OrderId, content: String, timePlaced: Long, isDone: Boolean)

class OrderId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object OrderId {
  private var counter = -1

  def apply(): OrderId = {
    counter += 1
    new OrderId(counter)
  }

  def apply(id: String): OrderId = {
    new OrderId(Integer.parseInt(id))
  }
}

class QueueExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the api.QueueRepository.
  */
trait OrderRepository {
  def create(data: OrderData)(implicit mc: MarkerContext): Future[OrderData]

  def get(id: OrderId)(implicit mc: MarkerContext): Future[Option[OrderData]]

  def list()(implicit mc: MarkerContext): Future[Iterable[OrderData]]

  def update(id: OrderId, newContent: String)(implicit mc: MarkerContext): Future[Option[OrderData]]

  def markDone(id: OrderId)(implicit mc: MarkerContext): Future[Option[OrderData]]

  def delete(id: OrderId)(implicit mc: MarkerContext): Future[Option[OrderData]]
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

  private val orderList = ListBuffer(
    OrderData(OrderId(), "dboll", Instant.now().getEpochSecond(), false),
    OrderData(OrderId(), "macka", Instant.now().getEpochSecond(), false),
    OrderData(OrderId(), "pölse", Instant.now().getEpochSecond(), false),
    OrderData(OrderId(), "matlåda", Instant.now().getEpochSecond(), false)
  )

  def create(data: OrderData)(implicit mc: MarkerContext): Future[OrderData] = {
    Future {
      orderList += data

      logger.trace(s"create: data = $data")
      data
    }
  }

  override def get(id: OrderId)(
    implicit mc: MarkerContext): Future[Option[OrderData]] = {
    Future {
      logger.trace(s"get: id = $id")
      orderList.find(order => order.id == id)
    }
  }

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[OrderData]] = {
    Future {
      logger.trace(s"list: ")
      orderList
    }
  }

  override def update(id: OrderId, newContent: String)(
    implicit mc: MarkerContext): Future[Option[OrderData]] = {
    Future {
      logger.trace(s"update: id = $id")
      orderList.find(order => order.id == id).map { maybeOrderData =>
        orderList -= maybeOrderData
        val toBeAdded = maybeOrderData.copy(content=newContent)
        orderList += toBeAdded
        toBeAdded
      }
    }
  }

  override def markDone(id: OrderId)(
    implicit mc: MarkerContext): Future[Option[OrderData]] = {
    Future {
      logger.trace(s"markDone: id = $id")
      orderList.find(order => order.id == id).map { maybeOrderData =>
        orderList -= maybeOrderData
        val toBeAdded = maybeOrderData.copy(isDone=true)
        orderList += toBeAdded
        toBeAdded
      }
    }
  }

  override def delete(id: OrderId)(
    implicit mc: MarkerContext): Future[Option[OrderData]] = {
    Future {
      logger.trace(s"delete: id = $id")
      orderList.find(order => order.id == id).map { maybeOrderData =>
        val toBeDeleted = maybeOrderData
        orderList -= toBeDeleted
        toBeDeleted
      }
    }
  }
}
