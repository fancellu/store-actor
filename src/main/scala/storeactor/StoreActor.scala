package storeactor

import akka.actor.Actor

import scala.reflect.ClassTag

object StoreActor {

  case object GETALL

  case class ADDITEM[T](t: T)

  case object IGNORE

  case object NORMAL

}

class StoreActor[T: ClassTag](var vec: Vector[T] = Vector.empty[T]) extends Actor {

  import StoreActor._

  private def addItem(item: T) = {
    vec = vec :+ item
  }

  val ignore: Receive = {
    case NORMAL => context.unbecome()
  }

  val normal: Receive = {
    case GETALL => sender() ! vec
    case ADDITEM(t: T) => addItem(t)
    case IGNORE => context.become(ignore, false)
    case unknown => println(s"Was not expecting $unknown : ${unknown.getClass}")
  }

  def receive: Receive = normal
}
