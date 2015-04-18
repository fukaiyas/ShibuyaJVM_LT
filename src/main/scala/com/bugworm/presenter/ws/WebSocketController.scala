package com.bugworm.presenter.ws

import java.net.URI
import javafx.application.Platform
import javax.websocket.{ClientEndpoint, ContainerProvider, OnMessage}

import com.bugworm.presenter.ActionProxy

import scalafx.scene.input.{SwipeEvent, TouchEvent}

object WebSocketController{
  val env = System.getenv("WS_PRESENTER_MODE")
  val defaultURI = "ws://com-bugworm-wsgradle.herokuapp.com/websocket_sample/"
//  val defaultURI = "ws://localhost:8080/websocket_sample/"
  val master = defaultURI + "sample/master"
  val child = defaultURI + "sample/child"
}

@ClientEndpoint
class MasterProxy extends ActionProxy {

  val uri = URI.create(WebSocketController.master)
  val session = ContainerProvider.getWebSocketContainer.connectToServer(this, uri)

  override def action(page : Int, num: Int) : Unit = {
    super.action(page, num)
    send("action:" + page + ":" + num)
  }
  override def moveTo(page : Int) : Unit = {
    super.moveTo(page)
    send("moveTo:" + page + ":0")
  }
  override def handleTouchReleased(page : Int) : Unit = {
    super.handleTouchReleased(page)
    send("touchReleased:" + page + ":0")
  }
  override def handleTouchMoved(page : Int, event : TouchEvent) : Unit = {
    super.handleTouchMoved(page, event)
    val p = event.getTouchPoint
    send("touchMoved:" + page + ":" + event.touchCount + ":" + p.getX + ":" + p.getY)
  }
  override def handleSwipeDown(page : Int, event : SwipeEvent) : Unit = {
    super.handleSwipeDown(page, event)
    send("swipeDown:" + page + ":" + event.touchCount)
  }

  def send(message : String) : Unit = {
    try{
      if(session.isOpen){
        session.getBasicRemote.sendText(message)
      }
    }catch{
      case e : Exception => e.printStackTrace()
      //TODO
    }
  }
}

@ClientEndpoint
class ChildProxy extends ActionProxy{

  val uri = URI.create(WebSocketController.child)
  val session = ContainerProvider.getWebSocketContainer.connectToServer(this, uri)

  @OnMessage
  def recieve(text : String): Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = message(text)
    })
  }
  def message(text : String): Unit = {
    val Array(command, page, param) = text.split(":", 3);
//    if(!"moveTo".equals(command) && page.toInt != presenter.currentPage){
//      presenter.moveTo(page.toInt)
//    }
    command match{
      case "action" =>
        controller.action(param.toInt)
        println(text)
      case "moveTo" => presenter.moveTo(page.toInt)
        println(text)
      case "touchReleased" => presenter.handleTouchReleased()
      case "touchMoved" =>
        val Array(count, x, y) = param.split(":")
        presenter.handleTouchMoved(count.toInt, x.toDouble, y.toDouble)
      case "swipeDown" =>
        presenter.handleSwipeDown(param.toInt)
      case _ => println("Err" + text)
    }
  }

  override def action(page : Int, num : Int) : Unit = {}
  override def moveTo(page : Int) : Unit = {}
  override def handleTouchReleased(page : Int) : Unit = {}
  override def handleTouchMoved(page : Int, event : TouchEvent) : Unit = {}
  override def handleSwipeDown(page : Int, event : SwipeEvent) : Unit = {}
}