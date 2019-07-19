package storeactor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import storeactor.StoreActor.{ADDITEM, GETALL}

import scala.concurrent.duration._

class TestKitExampleSpec extends TestKit(ActorSystem("ActorSystemTK"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll with Matchers {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "storeactor" should {
    "reply with items" in {
      val store = system.actorOf(Props(new StoreActor[Int]),"StoreActorTK1")

      store ! ADDITEM(1)
      store ! ADDITEM(2)
      store ! GETALL

      expectMsg(Vector(1,2))

      store ! ADDITEM(3)

      store ! GETALL

      val all=expectMsgType[Vector[Int]]

      all should be(Vector(1,2,3))

      system.stop(store)
    }

    "ignore rubbish" in {
      val store = system.actorOf(Props(new StoreActor[Int]),"StoreActorTK2")

      store ! "ignoreme"

      expectNoMessage(0.5.second)

      system.stop(store)
    }
  }

}
