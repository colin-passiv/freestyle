package freestyle.asyncMonix

import freestyle.async._

import monix.eval.Task
import monix.execution.Cancelable

object implicits {
  implicit val monixTaskAsyncContext = new AsyncContext[Task] {
    def runAsync[A](fa: Proc[A]): Task[A] = {
      Task.create((scheduler, callback) => {
        scheduler.execute(new Runnable {
          def run() =
            fa((result: Either[Throwable, A]) =>
              result match {
                case Right(v) => callback.onSuccess(v)
                case Left(ex) => callback.onError(ex)
            })
        })

        Cancelable.empty
      })
    }
  }
}
