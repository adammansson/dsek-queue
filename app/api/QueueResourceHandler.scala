package api

import play.api.MarkerContext
import play.api.libs.json._

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying Queue information.
  */
case class QueueResource(id: String, link: String, title: String, body: String)

object QueueResource {
  /**
    * Mapping to read/write a api.QueueResource out as a JSON value.
    */
    implicit val format: Format[QueueResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[QueueResource]]
  */
class QueueResourceHandler @Inject()(
                                     routerProvider: Provider[QueueRouter],
                                     QueueRepository: QueueRepository)(implicit ec: ExecutionContext) {

  def create(QueueInput: QueueFormInput)(
      implicit mc: MarkerContext): Future[QueueResource] = {
    val data = QueueData(QueueId("999"), QueueInput.title, QueueInput.body)
    // We don't actually create the Queue, so return what we have
    QueueRepository.create(data).map { id =>
      createQueueResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[QueueResource]] = {
    val QueueFuture = QueueRepository.get(QueueId(id))
    QueueFuture.map { maybeQueueData =>
      maybeQueueData.map { QueueData =>
        createQueueResource(QueueData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[QueueResource]] = {
    QueueRepository.list().map { QueueDataList =>
      QueueDataList.map(QueueData => createQueueResource(QueueData))
    }
  }

  private def createQueueResource(p: QueueData): QueueResource = {
    QueueResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}
