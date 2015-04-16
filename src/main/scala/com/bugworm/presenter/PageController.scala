package com.bugworm.presenter

abstract class PageController{
  var presenter : SfxPresenter = _
  var targetNode : javafx.scene.Node = _
  def init() : Unit = {
    presenter.rootPane.children.add(0, targetNode)
  }
  def action(num : Int) : Unit
  def dispose(): Unit = {
    presenter.rootPane.children.remove(targetNode)
  }
}

object DefaultController extends PageController {
  override def action(actionCount: Int): Unit = presenter.next()
}
