package storeactor

import akka.actor.Actor

import scala.reflect.ClassTag

// Like StoreActor, but no var usage
class ImmutableStoreActor[T: ClassTag](val initvec: Vector[T] = Vector.empty[T]) extends Actor {

  import StoreActor._

  val ignore: Receive = {
    case NORMAL => context.unbecome()
  }

  def normal(vec: Vector[T]) : Receive = {
    case GETALL => sender() ! vec
    case ADDITEM(t: T) => context.become(normal(vec :+ t))
    case IGNORE => context.become(ignore, false)
    case unknown => println(s"Was not expecting $unknown : ${unknown.getClass}")
  }

  def receive: Receive = normal(initvec)
}
