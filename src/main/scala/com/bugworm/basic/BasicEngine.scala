package com.bugworm.basic

import scala.util.parsing.combinator.RegexParsers

object BasicEngine extends RegexParsers{

  def parse(src : String) : ParseResult[AnyRef] = {
    parseAll(lines, src)
  }

  def lines : Parser[List[Line]] =  rep(line)

  def line : Parser[Line] = operation ^^ (new Line(_))

//  def multiOperation : Parser[Operation] = operation~rep(":"~>operation) ^^ {
//    case ope ~ multi => new Operation {
//      def operate(runtime: BasicRuntime, args : Value[Any]) : Unit = {
//        ope.execute(runtime)
//        for (mope <- multi if runtime.nextLine == -1 && !runtime.terminated) mope.execute(runtime)
//      }
//    }
//  }

  def operation : Parser[Operation] = new Parser[String] {
    def apply(in: Input) = {
      //TODO Parser[String]じゃなくて、命令の文字列と(あれば)カンマ区切りの引数を保持するクラスを作った方がよい？
      //      あ、タプルでいいか？
      //      で、命令の文字列がoperationsのキーに存在したら、引数も取得してSuccess、そうでなければFailure
      val source = in.source
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      var i = 0
      var j = start
      Success(source.subSequence(start, j).toString, in.drop(j - offset))
    }
  } ^^ {(s : String) =>
    new Operation() {
      override def operate(runtime: BasicRuntime, args: Seq[Value[Any]]): Unit = {
        operations(s)(runtime, args)
      }
    }
  }

  val operations : Map[String, Function2[BasicRuntime, Seq[Value[Any]], Unit]] = Map(
    "PRINT" -> {(r , v) => r.io.print(v(0)) : Unit}
  )
}

//TODO 複数命令対応
class Line(val operation : Operation)

trait Operation {
  def operate(runtime : BasicRuntime, args : Seq[Value[Any]]) : Unit
}

trait Value[T]
