package freestyle.http

import freestyle._

import cats.Monad

import scala.concurrent._

import _root_.play.api.mvc._
import _root_.play.api.http._

object play {
  object FreeSAction {
    def apply[F[_]](prog: FreeS[F, Result])(
        implicit MF: Monad[Future],
        I: ParInterpreter[F, Future],
        EC: ExecutionContext
    ): Action[AnyContent] = {
      Action.async {
        prog.exec[Future]
      }
    }

    def apply[A, F[_]](fn: Request[AnyContent] => FreeS[F, Result])(
        implicit MF: Monad[Future],
        I: ParInterpreter[F, Future],
        EC: ExecutionContext
    ): Action[AnyContent] = {
      Action.async { request =>
        val prog = fn(request)
        prog.exec[Future]
      }
    }
  }
}
