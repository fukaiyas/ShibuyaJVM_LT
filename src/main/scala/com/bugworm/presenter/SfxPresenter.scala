package com.bugworm.presenter

import javafx.fxml.FXMLLoader

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

  var controller : PageController = _

  var currentPage = 0

  var actionCount = 0

  var drawing : Option[Path] = None

  stage.initStyle(StageStyle.TRANSPARENT)
  stage.scene = new Scene(rootPane, slideWidth, slideHeight, fill){
    onKeyPressed = handleKeyEvent _
    onTouchMoved = handleTouchMoved _
    onSwipeDown = handleSwipeDown _
  }
  load()

  def load() : Unit = {
    val loader = new FXMLLoader(getClass.getResource(pages(currentPage)))
    val node = loader.load[javafx.scene.Node]()
    controller = Option(loader.getController[PageController]()).getOrElse(DefaultController)
    controller.presenter = this
    controller.targetNode = node
    controller.init()
    actionCount = 0
    rootPane.onMouseClicked = {event : MouseEvent =>
      event.clickCount match {
        case 2 =>
          actionCount += 1
          controller.action(actionCount)
        case _ =>
      }
    }
  }

  def moveTo(page : Int) : Unit = {
    if(pages.isDefinedAt(page)){
      controller.dispose()
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

  def handleSwipeDown(event : SwipeEvent) : Unit = {
    event.touchCount match {
      case 2 => drawing match {
        case None =>
          drawing = Option(new Path(){
            stroke = Color.Red
            strokeWidth = 12
            strokeLineCap = StrokeLineCap.ROUND
          })
          drawView.children.add(drawing.get)
          drawView.visible = true
        case Some(p) =>
          drawView.visible = false
          drawView.children.remove(p)
          drawing = None
      }
      case 4 => stage.close()
      case _ =>
    }
  }

  def handleTouchMoved(event : TouchEvent) : Unit = {
    event.touchCount match {
      case 1 =>
        val tp = event.touchPoint
        drawing.foreach(p => {
          p.elements.add(
            if (p.elements.isEmpty)
              MoveTo(tp.getX, tp.getY)
            else
              LineTo(tp.getX, tp.getY)
          )
        })
      case _ =>
    }
  }
}
