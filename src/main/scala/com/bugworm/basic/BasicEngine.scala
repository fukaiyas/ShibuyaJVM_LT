package com.bugworm.basic

import java.io.Reader

import scala.util.parsing.combinator.RegexParsers

object BasicEngine extends RegexParsers{

  def parse(src : String) : ParseResult[AnyRef] = {
    parseAll(lines, src)
  }

  def parse(reader : Reader) = {
    parseAll(lines, reader)
  }

  def lines : Parser[List[Line]] =  rep(line)

  def line : Parser[Line] = "^[1-9][0-9]{0,7}".r~operation ^^ {case ln~ope => new Line(ln.toInt, ope)}

  def operation : Parser[Operation] =
    "(REM|').*".r ^^^ Operation.nop |
    numvar~"="~numexpr ^^ {
      case va~c~ex => new Operation{
        override def operate(runtime : BasicRuntime) = runtime.decimalVars.put(va.name, ex(runtime))
      }
    } |
    strvar~"="~strexpr ^^ {
      case va~c~ex => new Operation{
        override def operate(runtime : BasicRuntime) = runtime.strVars.put(va.name, ex(runtime))
      }
    } |
    "IF"~>boolexpr~"THEN"~operation~opt("ELSE"~operation) ^^ {
      case bl~then~ope1~option => new Operation{
        override def operate(runtime : BasicRuntime) = {
          if(bl(runtime)){
            ope1.operate(runtime)
          }else{
            option.foreach{
              case el~ope2 => ope2.operate(runtime)
            }
          }
        }
      }
    } |
    "[A-Za-z]+".r~opt(value)~rep(","~>value) ^^ { case ope ~ val0 ~ vals  =>
      new Operation() {
        override def operate(runtime: BasicRuntime): Unit = {
          val arr : Array[Value[Any]] =
            if(val0.isEmpty) Array.empty[Value[Any]]
            else Array[Value[Any]](val0.get) ++ vals.toArray[Value[Any]]
          operations(ope)(runtime, arr)
        }
      }
    }

  def strexpr : Parser[Value[String]] = strval~rep("+"~>strval) ^^ {
    case head~tail => new Value[String]{
      def apply(runtime : BasicRuntime) : String = {
        (head(runtime) /: tail.map(_(runtime)))(_ + _)
      }
    }
  }

  def value : Parser[Value[Any]] = strexpr | strval | numexpr | numval | boolval

  def strval : Parser[Value[String]] = strfn | strvar | str

  def strfn : Parser[Function[String]] =
    "(?i)STR\\$\\(".r~>numexpr<~"\\)".r ^^ (n => new Function[String]{
      def apply(runtime : BasicRuntime) = n(runtime).toString()
    }) |
    "(?i)SCREEN\\$\\(".r~>numexpr~","~numexpr<~"\\)".r ^^ {
      case x~c~y => new Function[String]{
        def apply(runtime : BasicRuntime) : String = {
          runtime.io.screen(x(runtime), y(runtime))
        }
      }
    }

  def strvar : Parser[Var[String]] = "[A-Za-z][A-Za-z0-9_]*\\$".r ^^ (new Var[String](_){
    def apply(runtime : BasicRuntime) = runtime.strVars.getOrElse(name, "")
  })

  def str : Parser[Const[String]] = """"[^\"]*"""".r ^^ (s => new Const[String](s.tail.init))

  //数値系
  def numexpr : Parser[Value[BigDecimal]] = numterm~rep("+"~numterm | "-"~numterm) ^^ {
    case n~nums =>
      new Value[BigDecimal]{
        def apply(runtime : BasicRuntime) = {
          var v = n(runtime)
          nums.foreach{
            case "+"~t => v = v + t(runtime)
            case "-"~t => v = v - t(runtime)
          }
          v
        }
      }
  }

  def numterm : Parser[Value[BigDecimal]] = numval~rep("*"~numval | "/"~numval) ^^ {
    case n~nums =>
      new Value[BigDecimal]{
        def apply(runtime : BasicRuntime) = {
          var v = n(runtime)
          nums.foreach{
            case "*"~t => v = v * t(runtime)
            case "/"~t => v = v / t(runtime)
          }
          v
        }
      }
  }

  def numval : Parser[Value[BigDecimal]] = numfn | numvar | num

  def numfn : Parser[Function[BigDecimal]] =
    "(?i)STICK\\(".r~>numexpr<~"\\)".r ^^ (n => new Function[BigDecimal]{
      def apply(runtime : BasicRuntime) = runtime.io.stick(n(runtime))
    }) |
    "(?i)RND".r ^^^ {
      new Function[BigDecimal]{
        def apply(runtime : BasicRuntime) = BigDecimal(Math.random)
      }
    }

  def numvar : Parser[Var[BigDecimal]] = "[A-Za-z][A-Za-z0-9_]*".r ^^ (new Var[BigDecimal](_){
    def apply(runtime : BasicRuntime) = runtime.decimalVars.getOrElse(name, BigDecimal(0))
  })

  def num : Parser[Const[BigDecimal]] = """-?(\d+(\.\d*)?|\d*\.\d+)""".r ^^ (n => new Const[BigDecimal](BigDecimal(n)))

  //boolean
  def boolexpr : Parser[Value[Boolean]] = boolterm~rep("OR"~>boolterm) ^^ {
    case b~bools =>
      new Value[Boolean]{
        def apply(runtime : BasicRuntime) = {
          (b(runtime) /: bools.map(_(runtime)))(_ || _ )
        }
      }
  }

  def boolterm : Parser[Value[Boolean]] = boolval~rep("AND"~>boolval) ^^ {
    case b~bools =>
      new Value[Boolean]{
        def apply(runtime : BasicRuntime) = {
          (b(runtime) /: bools.map(_(runtime)))(_ && _ )
        }
      }
  }

  def boolval : Parser[Value[Boolean]] = boolfn | boolvar | bool

  def boolfn : Parser[Function[Boolean]] =
    numexpr~"==|<>|><|!=|<=|=<|>=|=>|>|<|=".r~numexpr ^^ {
      case num1~cp~num2 => new Function[Boolean]{
        def apply(runtime : BasicRuntime) = {
          cp match {
            case "=" | "==" => num1(runtime) == num2(runtime)
            case "<" => num1(runtime) < num2(runtime)
            case "<=" | "=<" => num1(runtime) <= num2(runtime)
            case ">" => num1(runtime) > num2(runtime)
            case ">=" | "=>" => num1(runtime) >= num2(runtime)
            case "<>" | "><" | "!=" => num1(runtime) != num2(runtime)
          }
        }
      }
    } |
      strexpr~"==|<>|><|!=|=".r~strexpr ^^ {
        case str1~cp~str2 => new Function[Boolean]{
          def apply(runtime : BasicRuntime) = {
            cp match {
              case "=" | "==" => str1(runtime) == str2(runtime)
              case "<>" | "><" | "!=" => str1(runtime) != str2(runtime)
            }
          }
        }
      }

  def boolvar : Parser[Var[Boolean]] = "[A-Za-z][A-Za-z0-9_]*\\?".r ^^ (new Var[Boolean](_){
    def apply(runtime : BasicRuntime) = runtime.booleanVars.getOrElse(name, false)
  })

  def bool : Parser[Const[Boolean]] = "(?i)true|false".r ^^ (n => new Const[Boolean](n.equalsIgnoreCase("true")))

  val operations : Map[String, Function2[BasicRuntime, Seq[Value[Any]], Unit]] = Map(
    "PRINT" -> {(r , v) => r.io.printStr(v(0)(r).toString, false) },
    "PRINTLN" -> {(r , v) => r.io.printStr(v(0)(r).toString, true) },
    "CYCLE" -> {(r, v) => r.io.cycle(BigDecimal(v(0)(r).toString), r) },
    "GOTO" -> {(r, v) => r.goto(v(0)(r).toString.toInt) },
    "SYNC" -> {(r, v) => r.sync = true },
    "END" -> {(r, v) => r.terminated = true },
    "LOCATE" -> {(r, v) => r.io.locate(BigDecimal(v(0)(r).toString), BigDecimal(v(1)(r).toString)) }
  )
}

//TODO 複数命令対応
class Line(val lineNumber : Integer, val operation : Operation){
  def execute(runtime : BasicRuntime) : Unit = {
    operation.operate(runtime)
  }
}
object Line {
  val end = new Line(999999, Operation.nop){
    def operate(runtime : BasicRuntime, args : Seq[Value[Any]]) = {
      runtime.terminated = true
      runtime
    }
  }
}

trait Value[+T]{
  def apply(runtime : BasicRuntime) : T
}

trait Function[T] extends Value[T]{
  def apply(runtime : BasicRuntime) : T
}

abstract class Var[T](val name : String) extends Value[T]{
  def apply(runtime : BasicRuntime) : T
}

class Const[T](val v : T)extends Value[T]{
  def apply(runtime : BasicRuntime) = v
}

trait Operation {
  def operate(runtime : BasicRuntime) : Unit
}
object Operation{
  val nop = new Operation(){
    def operate(runtime : BasicRuntime) : Unit = {}
  }
}
