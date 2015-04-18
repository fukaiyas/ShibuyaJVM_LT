package com.bugworm.basic

import java.io.{FileInputStream, InputStreamReader}
import javafx.event.ActionEvent
import javafx.event.EventHandler

import scalafx.animation.{KeyFrame, Timeline}
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, StackPane, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{FontWeight, Font}
import scalafx.Includes._

object BasicView {

  val model = new ScreenModel
  val sio = new ScreenIO(model)

  def create() : Pane = {

    val textscreen = new GridPane(){
      prefWidth = 640
      prefHeight = 400
      hgap = 0
      vgap = 0
    }

    for(x <- 0 until 40; y <- 0 until 25){
      textscreen.add(new Label(){
        textFill = Color.White
        font = Font.font("monospaced", FontWeight.Bold, 16)
        prefWidth = 16
        prefHeight = 16
        maxWidth = 16
        maxHeight = 16
        minWidth = 16
        minHeight = 16
        text <== model.text(x)(y)
      }, x, y)
    }

    new StackPane() {
      prefWidth = 640
      prefHeight = 400
      children = Seq(
        new Pane(){
          children = Seq(new Rectangle(){
            x <== model.cursorX * 16
            y <== model.cursorY * 16
            width = 16
            height = 16
            fill = Color.White
            visible = false
          })
        },
        textscreen
      )
    }
  }

  def start() : Unit = {
    val reader = new InputStreamReader(new FileInputStream("Sample.basic"), "UTF-8")
    val runtime = new BasicRuntime(BasicEngine.parse(reader).get)
    runtime.io = sio
    reader.close()
    runtime.cycle(BigDecimal.valueOf(50))
  }
}

class Loop(val frame : Double, val runtime : BasicRuntime) extends Timeline {

  var lastnano : Long = System.nanoTime
  cycleCount = Timeline.Indefinite
  keyFrames = KeyFrame(frame ms, "main loop [" + frame + "ms]", execute)
  def execute = new EventHandler[ActionEvent]{
    override def handle(event: ActionEvent): Unit = {
      val t = System.nanoTime
      lastnano = t
      runtime.sync = false
      var i = 0
      while(!runtime.sync && i < 100){
        runtime.lines.lift(runtime.currentLine).getOrElse(Line.end).execute(runtime)
        runtime.next()
        if(runtime.terminated){
          runtime.sync = true
          println("terminated")
          stop
        }
        i += 1
      }
      runtime.io.flush()
    }
  }
}
