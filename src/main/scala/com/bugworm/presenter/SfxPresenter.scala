package com.bugworm.presenter

import javafx.fxml.FXMLLoader
import javafx.scene.Node

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

  var actionProxy : ActionProxy = new DefaultControllerProxy()

  var currentPage = 0

  var actionCount = 0

  var lines : Option[Path] = None
  
  var drawing = false;

  stage.initStyle(StageStyle.TRANSPARENT)
  stage.scene = new Scene(rootPane, slideWidth, slideHeight, fill){
    onKeyPressed = handleKeyEvent _
    onTouchMoved = handleTouchMoved _
    onTouchReleased = handleTouchReleased _
    onSwipeLeft = handleSwipeLeft _
    onSwipeRight = handleSwipeRight _
    onSwipeDown = handleSwipeDown _
  }
  load()

  def load() : Unit = {
    val loader = new FXMLLoader(getClass.getResource(pages(currentPage)))
    val node = loader.load[javafx.scene.Node]()
    val controller = Option(loader.getController[PageController]()).getOrElse(DefaultController)
    println(controller.toString)
    controller.presenter = this
    controller.targetNode = node
    actionProxy.controller = controller
    actionProxy.init()
    actionCount = 0
    rootPane.onMouseClicked = {event : MouseEvent =>
      event.clickCount match {
        case 2 =>
          actionCount += 1
          actionProxy.action(actionCount)
        case _ =>
      }
    }
  }

  def moveTo(page : Int) : Unit = {
    if(pages.isDefinedAt(page)){
      actionProxy.dispose()
      currentPage = page
      load()
    }
  }
  def next() : Unit = moveTo(currentPage + 1)
  def prev() : Unit = moveTo(currentPage - 1)

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
  def handleSwipeDown(event : SwipeEvent) : Unit = {
    event.touchCount match {
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

  def handleTouchMoved(event : TouchEvent) : Unit = {
    event.touchCount match {
      case 1 =>
        val tp = event.touchPoint
        lines.foreach(p => {
          p.elements.add(
            if (drawing)
              LineTo(tp.getX, tp.getY)
            else
              MoveTo(tp.getX, tp.getY)
          )
          drawing = true
        })
      case _ =>
    }
  }

  def handleTouchReleased(event : TouchEvent) : Unit = {
    drawing = false
  }
}

trait ActionProxy{
  def controller_=(pageController : PageController) : Unit
  def controller : PageController

  def init() : Unit
  def action(num: Int) : Unit
  def dispose() : Unit
}
class DefaultControllerProxy extends ActionProxy{
  var target : PageController = _
  def controller_=(pageController : PageController) : Unit = target = pageController
  def controller : PageController = target

  override def init(): Unit = target.init()
  override def action(num: Int): Unit = target.action(num)
  override def dispose(): Unit = target.dispose()
}