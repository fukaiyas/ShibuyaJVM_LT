package com.bugworm.jvmlt.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}

import com.bugworm.presenter.PageController

class Intro extends PageController with Initializable  {

  @FXML
  var label1 : javafx.scene.control.Label = _
  @FXML
  var label2 : javafx.scene.control.Label = _
  @FXML
  var label3 : javafx.scene.control.Label = _

  override def initialize(location : URL, resources : ResourceBundle) : Unit = {
    label1.setVisible(false)
    label2.setVisible(false)
    label3.setVisible(false)
  }

  override def action(actionCount: Int): Unit = {
    actionCount match {
      case 1 =>
        label1.setVisible(true)
      case 2 =>
        label2.setVisible(true)
      case 3 =>
        label3.setVisible(true)
      case _ => presenter.moveTo(presenter.currentPage + 1)
    }
  }
}
