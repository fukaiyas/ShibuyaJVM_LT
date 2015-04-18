package com.bugworm.basic

import scalafx.beans.property.{StringProperty, IntegerProperty}

class ScreenModel {

  val width = 40

  val height = 25

  val cursorX : IntegerProperty = IntegerProperty(0)

  val cursorY : IntegerProperty  = IntegerProperty(0)

  //TODO やりすぎ
  val text : Array[Array[StringProperty]] = Array.fill(width, height)(new StringProperty)

  def print(st : String){
    st.foreach{
      c =>
        text(cursorX.value)(cursorY.value).value = c.toString
        addX()
    }
    println(st);
  }

  def newLine(){
    //TODO 改行コードをいれとくのかも?
    addY()
    cursorX.value = 0
  }

  def addX(){
    cursorX.value = cursorX.value + 1
    while(cursorX.value >= width){
      addY()
      cursorX.value = cursorX.value - width
    }
  }

  def addY(){
    cursorY.value = cursorY.value + 1
    if(cursorY.value >= height){
      for(x <- 0 until 40; y <- 0 until 24){
        if(text(x)(y).value != text(x)(y + 1).value){
          text(x)(y).value = text(x)(y + 1).value
        }
        //TODO 色が入ったら色情報も必要
      }
      for(x <- 0 until 40)text(x)(24).value = " "
      cursorY.value = height - 1
    }
  }
}
