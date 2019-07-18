package storeactor

import akka.actor.Actor

import scala.reflect.ClassTag

object StoreActor {
  case object GETALL
  case class ADDITEM[T](t: T)
}

class StoreActor[T: ClassTag](var vec: Vector[T] = Vector.empty[T]) extends Actor {

  import StoreActor.{ADDITEM, GETALL}

  private def addItem(item: T) = {
    vec = vec :+ item
  }

  def receive = {
    case GETALL => sender() ! vec
    case ADDITEM(t: T) => addItem(t)
    case unknown => println(s"Was not expecting $unknown : ${unknown.getClass}")
  }
}
