package api

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class QueueFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class QueueController @Inject()(cc: QueueControllerComponents)(
    implicit ec: ExecutionContext)
    extends QueueBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[QueueFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(QueueFormInput.apply)(QueueFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = QueueAction.async { implicit request =>
    logger.trace("index: ")
    QueueResourceHandler.find.map { Queues =>
      Ok(Json.toJson(Queues))
    }
  }

  def process: Action[AnyContent] = QueueAction.async { implicit request =>
    logger.trace("process: ")
    processJsonQueue()
  }

  def show(id: String): Action[AnyContent] = QueueAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      QueueResourceHandler.lookup(id).map { Queue =>
        Ok(Json.toJson(Queue))
      }
  }

  private def processJsonQueue[A]()(
      implicit request: QueueRequest[A]): Future[Result] = {
    def failure(badForm: Form[QueueFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: QueueFormInput) = {
      QueueResourceHandler.create(input).map { Queue =>
        Created(Json.toJson(Queue)).withHeaders(LOCATION -> Queue.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
