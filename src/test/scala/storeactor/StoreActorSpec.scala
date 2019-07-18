package storeactor

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import storeactor.StoreActor.{ADDITEM, GETALL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class StoreActorSpec extends WordSpec with Matchers {

  "storeactor" when {

    val system = ActorSystem("ActorSystem")

    val ITEMS = 100

    val duration: FiniteDuration = 2.second

    implicit val timeout: Timeout = duration

    s"when we add $ITEMS Int items from multiple threads" should {
      s"have length of $ITEMS and all items sent to it" in {
        val store = system.actorOf(Props(new StoreActor[Int]))

        val intlist = (1 to ITEMS).toList

        val futures = intlist.map { i => Future {store ! ADDITEM(i)}}

        val seqF: Future[List[Unit]] = Future.sequence(futures)

        Await.result(seqF, duration)

        val vecF = (store ? GETALL).mapTo[Vector[Int]]

        val vec = Await.result(vecF, duration)

        vec.size shouldBe ITEMS

        vec should contain theSameElementsAs intlist

        system.stop(store)
      }
    }

    s"when we add $ITEMS strings " should {
      s"have length of $ITEMS and all items sent to it" in {
        val store = system.actorOf(Props(new StoreActor[String]))

        val stringlist = (1 to ITEMS).map(_.toString)

        stringlist.foreach(i => store ! ADDITEM(i))

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec.size shouldBe ITEMS

        vec should contain theSameElementsInOrderAs stringlist

        system.stop(store)
      }
    }

    s"when we initialize with existing items " should {
      s"be there when get GETALL" in {
        val vector=Vector(1,2,3)

        val store = system.actorOf(Props(new StoreActor(vector)))

        store ! ADDITEM(4)

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec should contain theSameElementsInOrderAs vector :+ 4

        system.stop(store)
      }
    }

    s"when we send invalid messages " should {
      s"ignore them" in {
        val store = system.actorOf(Props(new StoreActor[String]))

        store ! ADDITEM("This is ok")
        store ! "this should be an ADDITEM"
        store ! ADDITEM(123)

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec.size shouldBe 1

        system.stop(store)
      }
    }

  }

}
