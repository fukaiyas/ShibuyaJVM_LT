package com.bugworm.jvmlt

abstract class PageController{
  var targetNode : javafx.scene.Node = _
  def init() : Unit = {
    JvmLtPresentation.rootPane.children.add(targetNode)
  }
  def action(num : Int) : Unit
  def dispose(): Unit = {
    JvmLtPresentation.rootPane.children.remove(targetNode)
  }
}

object DefaultController extends PageController {
  override def action(actionCount: Int): Unit = JvmLtPresentation.next()
}
