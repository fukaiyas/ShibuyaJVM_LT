package com.bugworm.basic

import scalafx.scene.input.{KeyCode, KeyEvent}

class ScreenIO(val model : ScreenModel) extends Io{
  val keys = Array(false, false, false, false)
  val keysupdate = keys.clone

  var loop : Option[Loop] = None

  def printStr(st : String, ln : Boolean){

    model.print(st)
    if(ln){
      model.newLine
    }
  }

  def input : String = {
    throw new UnsupportedOperationException
  }

  def color(f : BigDecimal){
    //TODO
  }

  def locate(x : BigDecimal, y : BigDecimal) {
    model.cursorX.value = x.intValue
    model.cursorY.value = y.intValue
  }

  def inkey = {
    throw new UnsupportedOperationException
  }

  def stick(n : BigDecimal) : BigDecimal = {
    val m = new Array[Boolean](4)
    for(i <- 0 until m.length)m(i) = keys(i) | keysupdate(i)
    m match {
      case Array(true, false, false, false) => BigDecimal(1)
      case Array(true, false, false, true) => BigDecimal(2)
      case Array(false, false, false, true) => BigDecimal(3)
      case Array(false, true, false, true) => BigDecimal(4)
      case Array(false, true, false, false) => BigDecimal(5)
      case Array(false, true, true, false) => BigDecimal(6)
      case Array(false, false, true, false) => BigDecimal(7)
      case Array(true, false, true, false) => BigDecimal(8)
      case _ => BigDecimal(0)
    }
  }

  def screen(x : BigDecimal, y : BigDecimal) : String = {

    model.text(x.intValue)(y.intValue).value
  }

  def cycle(n : BigDecimal, runtime : BasicRuntime){

    loop.foreach(_.stop)
    loop = Option(new Loop(n.doubleValue(), runtime))
    loop.get.play()
  }

  def flush(){
    for(i <- 0 until keysupdate.length)keysupdate(i) = false
  }

  def keyPressed(event : KeyEvent) {

    event.code match{
      case KeyCode.UP =>
        keys(0) = true
        keysupdate(0) = true
      case KeyCode.DOWN =>
        keys(1) = true
        keysupdate(1) = true
      case KeyCode.LEFT =>
        keys(2) = true
        keysupdate(2) = true
      case KeyCode.RIGHT =>
        keys(3) = true
        keysupdate(3) = true
      case _ =>
    }
  }

  def keyReleased(event : KeyEvent){

    event.code match{
      case KeyCode.UP =>
        keys(0) = false
      case KeyCode.DOWN =>
        keys(1) = false
      case KeyCode.LEFT =>
        keys(2) = false
      case KeyCode.RIGHT =>
        keys(3) = false
      case _ =>
    }
  }

  def keyTyped(event : KeyEvent) {
    //TODO
  }
}
