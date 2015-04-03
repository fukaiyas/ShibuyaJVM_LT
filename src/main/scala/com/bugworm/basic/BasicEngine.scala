package com.bugworm.basic

import scala.util.parsing.combinator.RegexParsers

object BasicEngine extends RegexParsers{

  def parse(src : String) : Unit = {
    parseAll(lines, src)
  }

  def lines : Parser[List[Line]] =  rep(line)

  def line : Parser[Line] = null
}

class Line