package com.bugworm.jvmlt

import com.bugworm.presenter.SfxPresenter

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

object JvmLtPresentationChild extends JFXApp{

  val pages = Array(
    "/001_title.fxml",
    "/002_agenda.fxml",
    "/003_basic.fxml",
    "/004_parser.fxml",
    "/005_bnf.fxml",
    "/006_scalafx.fxml",
    "/007_demo.fxml",
    "/999_end.fxml")

  System.setProperty("action.proxy", "child")
  new SfxPresenter(new PrimaryStage(), pages)
}
