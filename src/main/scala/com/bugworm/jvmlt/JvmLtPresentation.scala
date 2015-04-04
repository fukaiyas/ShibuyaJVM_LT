package com.bugworm.jvmlt

import com.bugworm.presenter.SfxPresenter

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

object JvmLtPresentation extends JFXApp{

  //TODO
  val pages = Array("/001_title.fxml", "/002.fxml", "/003.fxml", "/004.fxml")

  new SfxPresenter(new PrimaryStage(), pages)
}
