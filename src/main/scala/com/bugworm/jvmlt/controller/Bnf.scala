package com.bugworm.jvmlt.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}

import com.bugworm.basic.BasicView
import com.bugworm.presenter.PageController

class Bnf extends PageController with Initializable {

  @FXML
  var bnf1 : javafx.scene.control.Label = _
  @FXML
  var bnf2 : javafx.scene.control.Label = _
  @FXML
  var bnf3 : javafx.scene.control.Label = _

  @FXML
  var src : javafx.scene.control.Label = _

  @FXML
  var rect : javafx.scene.layout.Pane = _

  override def initialize(location : URL, resources : ResourceBundle) : Unit = {
    src.setVisible(false)
    rect.setVisible(false)
  }

  override def action(actionCount: Int): Unit = {
    actionCount match {
      case 1 => {
        bnf2.setVisible(false)
        bnf3.setVisible(false)
        src.setVisible(true)
      }
      case 2 => rect.setVisible(true)
      case _ => presenter.next()
    }
  }
}
