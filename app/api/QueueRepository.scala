package api

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

final case class QueueData(id: QueueId, title: String, body: String)

class QueueId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object QueueId {
  def apply(raw: String): QueueId = {
    require(raw != null)
    new QueueId(Integer.parseInt(raw))
  }
}

class QueueExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the api.QueueRepository.
  */
trait QueueRepository {
  def create(data: QueueData)(implicit mc: MarkerContext): Future[QueueId]

  def list()(implicit mc: MarkerContext): Future[Iterable[QueueData]]

  def get(id: QueueId)(implicit mc: MarkerContext): Future[Option[QueueData]]
}

/**
  * A trivial implementation for the Queue Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class QueueRepositoryImpl @Inject()()(implicit ec: QueueExecutionContext)
    extends QueueRepository {

  private val logger = Logger(this.getClass)

  private val QueueList = List(
    QueueData(QueueId("1"), "title 1", "blog Queue 1"),
    QueueData(QueueId("2"), "title 2", "blog Queue 2"),
    QueueData(QueueId("3"), "title 3", "blog Queue 3"),
    QueueData(QueueId("4"), "title 4", "blog Queue 4"),
    QueueData(QueueId("5"), "title 5", "blog Queue 5")
  )

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[QueueData]] = {
    Future {
      logger.trace(s"list: ")
      QueueList
    }
  }

  override def get(id: QueueId)(
      implicit mc: MarkerContext): Future[Option[QueueData]] = {
    Future {
      logger.trace(s"get: id = $id")
      QueueList.find(Queue => Queue.id == id)
    }
  }

  def create(data: QueueData)(implicit mc: MarkerContext): Future[QueueId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
