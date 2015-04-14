package com.bugworm.basic

class BasicRuntime {

  def io() : Io = {
    null //TODO
  }
}

trait Io {

  def print(m : Any) : Unit
}
