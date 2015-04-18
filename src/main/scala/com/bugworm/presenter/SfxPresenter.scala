package com.bugworm.presenter

import javafx.fxml.FXMLLoader
import javafx.scene.Node

import com.bugworm.presenter.ws.{ChildProxy, MasterProxy}

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.input._
import scalafx.scene.layout.{Pane, StackPane}
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.shape._
import scalafx.stage.{Stage, StageStyle}

class SfxPresenter(
  val stage : Stage, 
  val pages : Array[String], 
  val slideWidth : Double, 
  val slideHeight : Double,
  val fill : Paint) {

  def this(stage : Stage, pages : Array[String]) = this(stage, pages, 1024, 768, Color(0, 0, 0, 0))

  val drawView = new Pane(){
    prefWidth = slideWidth
    prefHeight = slideHeight
    children = Polygon(0, 0, 32, 0, 0, 32)
    visible = false
  }

  val rootPane = new StackPane(){
    children = drawView
  }

  var actionProxy : ActionProxy = System.getProperty("action.proxy") match {
    case "master" => new MasterProxy()
    case "child" => new ChildProxy()
    case _ => new ActionProxy()
  }

  var currentPage = 0

  var actionCount = 0

  var lines : Option[Path] = None
  
  var drawing = false

  actionProxy.presenter = this
  stage.initStyle(StageStyle.TRANSPARENT)
  stage.scene = new Scene(rootPane, slideWidth, slideHeight, fill){
    onKeyPressed = handleKeyEvent _
    onTouchMoved =(e : TouchEvent) => actionProxy.handleTouchMoved(currentPage, e)
    onTouchReleased = handle{actionProxy.handleTouchReleased(currentPage)}
    onSwipeLeft = handleSwipeLeft _
    onSwipeRight = handleSwipeRight _
    onSwipeDown = (e : SwipeEvent) => actionProxy.handleSwipeDown(currentPage, e)
  }
  load()

  def load() : Unit = {
    val loader = new FXMLLoader(getClass.getResource(pages(currentPage)))
    val node = loader.load[javafx.scene.Node]()
    val controller = Option(loader.getController[PageController]()).getOrElse(DefaultController)
    controller.presenter = this
    controller.targetNode = node
    actionProxy.controller = controller
    actionProxy.controller.init()
    actionCount = 0
    rootPane.onMouseClicked = {event : MouseEvent =>
      event.clickCount match {
        case 2 =>
          actionCount += 1
          actionProxy.action(currentPage, actionCount)
        case _ =>
      }
    }
  }

  def moveTo(page : Int) : Unit = {
    if(pages.isDefinedAt(page)){
      println("::::::moveto")
      actionProxy.controller.dispose()
      currentPage = page
      load()
    }
  }

  def next() : Unit = actionProxy.moveTo(currentPage + 1)
  def prev() : Unit = actionProxy.moveTo(currentPage - 1)

  def handleKeyEvent(event : KeyEvent) : Unit = {
    event.code match {
      case KeyCode.Q | KeyCode.ESCAPE => stage.close()
      case KeyCode.LEFT => prev()
      case KeyCode.RIGHT => next()
      case _ =>
    }
  }

  def handleSwipeLeft(event : SwipeEvent) : Unit = {
    event.touchCount match {
      case 2 => next()
      case _ =>
    }
  }
  def handleSwipeRight(event : SwipeEvent) : Unit = {
    event.touchCount match {
      case 2 => prev()
      case _ =>
    }
  }
  def handleSwipeDown(touchCount : Int) : Unit = {
    println("Swipedown")
    touchCount match {
      case 2 => lines match {
        case None =>
          lines = Option(new Path(){
            stroke = Color.Red
            strokeWidth = 12
            strokeLineCap = StrokeLineCap.ROUND
          })
          drawView.children.add(lines.get)
          drawView.visible = true
        case Some(p) =>
          drawView.visible = false
          drawView.children.remove(p)
          lines = None
      }
      case 4 => stage.close()
      case _ =>
    }
  }

  def handleTouchMoved(touchCount : Int, touchX : Double, touchY : Double) : Unit = {
    touchCount match {
      case 1 =>
        lines.foreach(p => {
          p.elements.add(
            if (drawing)
              LineTo(touchX, touchY)
            else
              MoveTo(touchX, touchY)
          )
          drawing = true
        })
      case _ =>
    }
  }

  def handleTouchReleased() : Unit = {
    drawing = false
  }
}

class ActionProxy{
  var sfxpr : SfxPresenter = _
  def presenter_=(pr : SfxPresenter) : Unit = sfxpr = pr
  def presenter : SfxPresenter = sfxpr
  var target : PageController = _
  def controller_=(pageController : PageController) : Unit = target = pageController
  def controller : PageController = target

  def action(page : Int, num : Int) : Unit = target.action(num)
  def moveTo(page : Int) : Unit = presenter.moveTo(page)
  def handleTouchReleased(page : Int) : Unit = presenter.handleTouchReleased()
  def handleTouchMoved(page : Int, event : TouchEvent) : Unit = presenter.handleTouchMoved(event.touchCount, event.touchPoint.getX, event.touchPoint.getY)
  def handleSwipeDown(page : Int, event : SwipeEvent) : Unit = presenter.handleSwipeDown(event.touchCount)
  //TODO handleSwipeDown
}
