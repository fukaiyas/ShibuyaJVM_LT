package com.bugworm.basic

import com.sun.org.apache.bcel.internal.classfile.LineNumber

import scala.collection.mutable.HashMap

class BasicRuntime(val lines : List[Line]) {
  val strVars : HashMap[String, String] = new HashMap[String, String]
  val decimalVars : HashMap[String, BigDecimal] = new HashMap[String, BigDecimal]
  val booleanVars : HashMap[String, Boolean] = new HashMap[String, Boolean]
  var io : Io = new ScreenIO(new ScreenModel())
  var sync : Boolean = false
  var terminated : Boolean = false
  var currentLine : Int = 0
  var nextLine : Int = -1

  def next() = {
    currentLine = if(nextLine == -1) currentLine + 1 else nextLine
    nextLine = -1
  }

  def goto(lineNumber: Integer) : Unit = {
    for(i <- 0 until lines.size if lines(i).lineNumber == lineNumber){
      nextLine = i
    }
  }

  def input(ex : Var[String]): Unit ={
    //TODO
  }

  def cycle(n : BigDecimal){
    io.cycle(n, this)
  }
}

trait Io {

  def printStr(st : String, ln : Boolean) : Unit
  def input : String
  def color(f : BigDecimal) : Unit
  def locate(x : BigDecimal, y : BigDecimal) : Unit

  def inkey : String
  def stick(n : BigDecimal) : BigDecimal
  def screen(x : BigDecimal, y : BigDecimal) : String

  def cycle(n : BigDecimal, runtime : BasicRuntime) : Unit
  def flush() : Unit
}
