package storeactor

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import storeactor.StoreActor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class StoreActorSpec extends WordSpec with Matchers {

  "storeactor" when {

    implicit val system = ActorSystem("ActorSystem")

    val ITEMS = 100

    val duration: FiniteDuration = 2.second

    implicit val timeout: Timeout = duration

    s"when we add $ITEMS Int items from multiple threads" should {
      s"have length of $ITEMS and all items sent to it" in {
        val store = system.actorOf(Props(new StoreActor[Int]),"StoreActor1")

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
        val store = system.actorOf(Props(new StoreActor[String]),"StoreActor2")

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

        val store = system.actorOf(Props(new StoreActor(vector)),"StoreActor3")

        store ! ADDITEM(4)

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec should contain theSameElementsInOrderAs vector :+ 4

        system.stop(store)
      }
    }

    s"when we send invalid messages " should {
      s"ignore them" in {
        val store = system.actorOf(Props(new StoreActor[String]),"StoreActor4")

        store ! ADDITEM("This is ok")
        store ! "this should be an ADDITEM"
        store ! ADDITEM(123)

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec.size shouldBe 1

        system.stop(store)
      }
    }

    "messages from scheduler" should {
     "see some" in {
       val store = system.actorOf(Props(new StoreActor[String]),"StoreActor6")
       system.scheduler.schedule(0.milli,20.milli){
         store ! ADDITEM("from scheduler")
       }
       Thread.sleep(100)

       val vecF = (store ? GETALL).mapTo[Vector[String]]

       val vec = Await.result(vecF, duration)

       vec.size should be > 3
     }
    }

    "cancelled messages from scheduler" should {
      "see none" in {
        val store = system.actorOf(Props(new StoreActor[String]),"StoreActor7")
        val cancellable=system.scheduler.schedule(1.second,20.milli){
          store ! ADDITEM("from scheduler")
        }
        Thread.sleep(100)
        cancellable.cancel()

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec.size shouldBe 0
      }
    }

    "when we send ignore message" should {
      "ignore following" in {
        val store = system.actorOf(Props(new StoreActor[String]),"StoreActor5")

        store ! ADDITEM("not ignored1")
        store ! IGNORE
        store ! ADDITEM("ignored1")
        store ! ADDITEM("ignored2")
        store ! NORMAL
        store ! ADDITEM("not ignored2")

        val vecF = (store ? GETALL).mapTo[Vector[String]]

        val vec = Await.result(vecF, duration)

        vec.size shouldBe 2
        vec should contain theSameElementsInOrderAs List("not ignored1", "not ignored2")

        system.stop(store)
      }
    }

    "synchronous test: when we send ignore message" should {
      "ignore following" in {
        // is invoked in calling thread, and allows deep inspection of Actor state
        val store = TestActorRef[StoreActor[String]](Props(new StoreActor[String]))

        // you could even say store.receive(ADDITEM("not ignored1"))

        store ! ADDITEM("not ignored1")
        store ! IGNORE
        store ! ADDITEM("ignored1")
        store ! ADDITEM("ignored2")
        store ! NORMAL
        store ! ADDITEM("not ignored2")

        val vec = store.underlyingActor.vec

        vec.size shouldBe 2
        vec should contain theSameElementsInOrderAs List("not ignored1", "not ignored2")

        system.stop(store)
      }
    }

  }

}
