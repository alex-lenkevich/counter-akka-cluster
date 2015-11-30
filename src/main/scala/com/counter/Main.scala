package com.counter

import akka.actor._

object Main extends App {

  val nodeConfig = NodeConfig parse args

  // If a config could be parsed - start the system
  nodeConfig foreach { c =>
    val system = ActorSystem(c.clusterName, c.config)
    // Register a monitor actor for demo purposes
    val nodeNumber: Int = c.config.getInt("akka.cluster.node.number")
    val nodeCount: Int = c.config.getInt("akka.cluster.node.count")
    val nodeSecret: Int = c.config.getInt("akka.cluster.node.secret")
    system.actorOf(Props(classOf[Counter], nodeNumber, nodeCount, nodeSecret), s"counter_$nodeNumber")
    system.log info s"ActorSystem ${system.name} started successfully, subscribed: counter_$nodeNumber"
  }

}