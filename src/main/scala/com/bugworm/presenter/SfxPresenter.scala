package com.bugworm.presenter

import javafx.fxml.FXMLLoader

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.stage.{StageStyle, Stage}

class SfxPresenter(val stage : Stage, val pages : Array[String]) {

  val rootPane = new StackPane()

  var controller : PageController = _

  var currentPage = 0

  var actionCount = 0

  stage.initStyle(StageStyle.TRANSPARENT)
  stage.scene = new Scene(rootPane, 1024, 768, Color(0, 0, 1, 0.01)){  //TODO サイズと背景色は指定可能にする
    onKeyPressed = handleKeyEvent _
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
    rootPane.onMouseClicked = handle{
      actionCount += 1
      controller.action(actionCount)
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
}
