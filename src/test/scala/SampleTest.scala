import scala.util.parsing.combinator.RegexParsers

object SampleTest  extends RegexParsers{

  def main(args : Array[String]) : Unit = {
    parse("");
  }

  def parse(data : String) : Unit = {
    parseAll(some1, data);
  }

  def some1 : Parser[Boolean] = "hello".r ^^ {(s : String) => true}
  def some2 : Parser[Boolean] = "hello".r ^^^ true
  // ^^ は左辺が成功したらその結果を受け取ってParser[T]のTを返す関数を右辺に指定
  // ^^^ は左辺が成功したら結果は捨てて、Parser[T]のTそのものを右辺に指定
}
