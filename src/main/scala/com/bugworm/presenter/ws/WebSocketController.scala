package com.bugworm.presenter.ws

import java.net.URI
import javax.websocket.{ClientEndpoint, ContainerProvider}

import com.bugworm.presenter.{ActionProxy, PageController}

object WebSocketController{
  val env = System.getenv("WS_PRESENTER_MODE")
  val defaultURI = "ws://com-bugworm-wsgradle.herokuapp.com/websocket_sample/"
  val master = defaultURI + "sample/child"
  val child = defaultURI + "sample/master"
  val uri = URI.create(if("master".equalsIgnoreCase(env)) master else child)
}

@ClientEndpoint
class MasterProxy extends ActionProxy {

  val session = ContainerProvider.getWebSocketContainer.connectToServer(this, WebSocketController.uri)
  var localTarget : PageController = _
  override def controller_=(pageController: PageController): Unit = localTarget = pageController
  override def controller: PageController = localTarget

  override def init() : Unit = {
    localTarget.init()
    send("init")
  }
  override def action(num: Int) : Unit = {
    localTarget.action(num)
    send("action:" + num)
  }
  override def dispose() : Unit = {
    localTarget.dispose()
    send("dispose")
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

//TODO ChildProxy