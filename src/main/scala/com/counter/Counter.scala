package com.counter

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}

import akka.actor._
import akka.cluster._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import com.counter.Counter.Msg

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.Failure

class Counter(nodeNumber: Int, nodeCount: Int, nodeSecret: Int) extends Actor with ActorLogging {

  def topic(i: Int) = s"counter_$i"

  var SelfTopic = topic(nodeNumber)

  val cluster = Cluster(context.system)

  val mediator = DistributedPubSub(cluster.system).mediator

  mediator ! Put(self)

  def receive = {
    case Msg(x) => buffer.put(x)
    case x: Any => log.info(s"unexpected $x")
  }

  override def preStart(): Unit = {
    cluster.registerOnMemberUp {
      Thread.sleep(5000)
      log.info(s"start")
      process()
    }
  }

  def process() = {

    val children = List(nodeNumber * 2, nodeNumber * 2 + 1).filter(nodeCount >=)
    val parent: Option[Int] = if(nodeNumber > 1) Some(nodeNumber / 2) else None

    def receiveNum = (_: Any) => {
      blocking {
        receiveFromANode()
      }
    }
    def receiveAndAccumulate = (num: Int) => {
      receiveNum() + num
    }

    val broadcastChildren = (sum: Int) => {
      children.foreach(node => sendToANode(node, sum))
      sum
    }

    // TODO: I'd like to write it in slacaz instead
    (children.foldLeft(Future(nodeSecret)) { (f, _) => f map receiveAndAccumulate } map {
      x => parent.map { parent =>
        sendToANode(parent, x)
        receiveNum()
      }.getOrElse(x)
    }) map broadcastChildren andThen {
      case x: Failure[Int] => log.error(x.exception, "Fail")
      case scala.util.Success(x) => log.info(s"Sum is $x")
    }
  }

  var buffer: BlockingQueue[Int] = new LinkedBlockingQueue[Int]

  def receiveFromANode() : Int = {
    log.info("waiting for message")
    buffer.poll(1, TimeUnit.HOURS)
  }

  def sendToANode(target: Int, message: Int) = {
    log.info(s"sending message $message to $target")
    // TODO: Send only if node is up and resend message if it's not delivered
    mediator ! Send(path = s"/user/counter_$target", msg = Msg(message), localAffinity = false)
  }

}

object Counter {
  case class Msg(num: Int)
}