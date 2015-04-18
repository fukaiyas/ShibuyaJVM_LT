package com.bugworm.jvmlt.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}

import com.bugworm.basic.BasicView
import com.bugworm.presenter.PageController

class Demo extends PageController with Initializable{

  @FXML
  var demoPane : javafx.scene.layout.Pane = _

  @FXML
  var code : javafx.scene.control.TextArea = _

  override def initialize(location : URL, resources : ResourceBundle) : Unit = {
    demoPane.getChildren.add(BasicView.create())
  }

  override def action(actionCount: Int): Unit = {
    actionCount match {
      case 1 => {
        code.setVisible(false)
        BasicView.start()
      }
      case _ => presenter.next()
    }
  }

  override def dispose(): Unit = {
    presenter.rootPane.children.remove(targetNode)
    try{
      BasicView.runtime.terminated = true;
    }catch{
      case e : Exception => e.printStackTrace()
    }
  }
}
