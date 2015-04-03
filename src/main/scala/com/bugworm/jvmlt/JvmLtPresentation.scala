package com.bugworm.jvmlt

import javafx.fxml.FXMLLoader

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.stage.StageStyle

object JvmLtPresentation extends JFXApp{

  val rootPane = new StackPane

  //TODO
  val pages = Array("/001_title.fxml", "/002.fxml", "/003.fxml", "/004.fxml")

  var controller : PageController = _

  var currentPage = 0

  var actionCount = 0

  stage = new PrimaryStage{
    initStyle(StageStyle.TRANSPARENT)
    scene = new Scene(rootPane, 1024, 768, Color(0, 0, 1, 0.01))
  }
  load()

  def load() : Unit = {
    val loader = new FXMLLoader(getClass().getResource(pages(currentPage)))
    val node = loader.load[javafx.scene.Node]()
    controller = Option(loader.getController[PageController]()).getOrElse(DefaultController)
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
}
