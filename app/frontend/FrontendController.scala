package frontend

import play.api.Logger
import play.api.mvc._

import javax.inject._

/**
 * Takes HTTP requests and displays web pages.
 */
@Singleton
class FrontendController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  private val logger = Logger(getClass)

  def index: Action[AnyContent] = Action { implicit request =>
    logger.trace("index: ")
    Ok(html.index("Welcome"))
  }
}
