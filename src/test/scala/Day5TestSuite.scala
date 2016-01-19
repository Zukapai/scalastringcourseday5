package day5

import java.nio.charset._
import java.nio.{ByteBuffer, CharBuffer}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}
import java.util.{Date, StringJoiner}

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

/**
  * @author ynupc
  *         Created on 2016/01/19
  */
class Day5TestSuite extends AssertionsForJUnit {

  @Test
  def testStringUnion(): Unit = {
    val str = "A"

    assert(str + "B" == "AB")
  }

  @Test
  def testStringConcat(): Unit = {
    val str = "A"

    assert(str.concat("B") == "AB")
  }

  @Test
  def testStringBuffer(): Unit = {
    var buffer: StringBuffer = new StringBuffer()

    assert(buffer.capacity == 16)
    assert(buffer.length == 0)

    buffer = new StringBuffer(3)

    assert(buffer.capacity == 3)
    assert(buffer.length == 0)

    val array: Array[String] = Array[String]("abc", "cde", "efg")
    for (i <- array.indices) {
      i match {
        case 0 =>
          buffer.append(array(i))
          assert(buffer.capacity == 3)
        case 1 =>
          buffer.append(array(i))
          assert(buffer.capacity == 8)
        case 2 =>
          buffer.append(array(i))
          assert(buffer.capacity == 18)
        case otherwise =>
          //Do nothing
      }
    }

    assert(buffer.capacity == 18)

    var str: String = buffer.toString
    assert(str == "abccdeefg")

    assert(buffer.length == 9)

    buffer.setLength(0)

    assert(buffer.capacity == 18)
    assert(buffer.length == 0)

    str = buffer.toString
    assert(str == "")
  }

  @Test
  def testStringBuilder1(): Unit = {
    var builder: StringBuilder = new StringBuilder()

    assert(builder.capacity == 16)
    assert(builder.length == 0)

    builder = new StringBuilder(3)

    assert(builder.capacity == 3)
    assert(builder.length == 0)

    val array: Array[String] = Array[String]("abc", "cde", "efg")
    for (element <- array) {
      builder.append(element)
    }
    var str: String = builder.result
    assert(str == "abccdeefg")

    assert(builder.length == 9)

    builder.setLength(0)

    assert(builder.capacity == 18)
    assert(builder.length == 0)

    str = builder.result
    assert(str == "")

    for (element <- array) {
      builder.append(element)
    }

    assert(builder.capacity == 18)
    assert(builder.length == 9)

    //clearメソッドはJavaにはないが、中身はsetLength(0)
    builder.clear

    assert(builder.capacity == 18)
    assert(builder.length == 0)

    for (element <- array) {
      builder.append(element)
    }

    builder.delete(0, builder.length)

    assert(builder.capacity == 18)
    assert(builder.length == 0)
  }

  @Test
  def testStringBuilder2(): Unit = {
    var builder: java.lang.StringBuilder = new java.lang.StringBuilder()

    assert(builder.capacity == 16)
    assert(builder.length == 0)

    builder = new java.lang.StringBuilder(3)

    assert(builder.capacity == 3)
    assert(builder.length == 0)

    val array: Array[String] = Array[String]("abc", "cde", "efg")
    for (element <- array) {
      builder.append(element)
    }
    var str: String = builder.toString
    assert(str == "abccdeefg")

    assert(builder.length == 9)

    builder.setLength(0)

    assert(builder.capacity == 18)
    assert(builder.length == 0)

    str = builder.toString
    assert(str == "")
  }


  @Test
  def testStringJoiner1(): Unit = {
    val array: Array[String] = Array[String]("abc", "cde", "efg")
    val joiner: StringJoiner = new StringJoiner(", ", "[", "]")
    array foreach {
      element =>
        joiner.add(element)
    }
    assert(joiner.toString == "[abc, cde, efg]")
  }

  @Test
  def testStringJoiner2(): Unit = {
    val array = Array[String]("abc", "cde", "efg")
    val joiner = new StringJoiner(", ")
    array foreach {
      element =>
        joiner.add(element)
    }
    assert(joiner.toString == "abc, cde, efg")
  }

  @Test
  def testStringJoin1(): Unit = {
    val array = Array[String]("abc", "cde", "efg")
    assert(String.join(", ", array: _*) == "abc, cde, efg")
    assert(String.join(", ", array: _*).mkString("[", "", "]") == "[abc, cde, efg]")
  }

  @Test
  def testStringJoin2(): Unit = {
    val array = Array[String]("abc", "cde", "efg")
    val iterable: java.lang.Iterable[String] = scala.collection.JavaConversions.asJavaIterable(array.toIterable)
    assert(String.join(", ", iterable) == "abc, cde, efg")
    assert(String.join(", ", iterable).mkString("[", "", "]") == "[abc, cde, efg]")
  }

  private val utf8ByteArray1ForBufferTest: Array[Byte] =
    for (byte <- Array[Int](
      0xF0, 0xA0, 0xAE, 0xB7,//𠮷=U+20BB7=U+D842,U+DFB7
      0xE9, 0x87, 0x8E,//野=U+91CE
      0xE5, 0xAE, 0xB6,//家=U+5BB6
      0x00, 0x00, 0x00,
      0x00, 0x00, 0x00,
      0x00, 0x00, 0x00
    )) yield byte.toByte

  private val utf8ByteArray2ForBufferTest: Array[Byte] =
    for (byte <- Array[Int](
      0xF0, 0xA0, 0xAE, 0xB7,//𠮷=U+20BB7=U+D842,U+DFB7
      0xE9, 0x87, 0x8E,//野=U+91CE
      0xE5, 0xAE, 0xB6 //家=U+5BB6
    )) yield byte.toByte

  private val capacity = 16

  private val utf8ByteArray3ForBufferTest: Array[Byte] =
    {
      for (i <- 0 until capacity) yield {
        if (i < utf8ByteArray2ForBufferTest.length) {
          utf8ByteArray2ForBufferTest(i)
        } else {
          0: Byte
        }
      }
    }.toArray

  @Test
  def testBufferForEncoder1(): Unit = {
    val str = "𠮷野家"
    val encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder.
      onMalformedInput(CodingErrorAction.REPORT).
      onUnmappableCharacter(CodingErrorAction.REPORT)
    val charBuffer: CharBuffer = CharBuffer.wrap(str)
    val byteBuffer: ByteBuffer = encoder.encode(charBuffer)
    val byteArray: Array[Byte] = byteBuffer.array
    assert(byteArray sameElements utf8ByteArray1ForBufferTest)
  }

  @Test
  def testBufferForEncoder2(): Unit = {
    val str = "𠮷野家"
    val encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder.
      onMalformedInput(CodingErrorAction.REPORT).
      onUnmappableCharacter(CodingErrorAction.REPORT)
    val charBuffer: CharBuffer = CharBuffer.wrap(str)
    val byteBuffer: ByteBuffer = ByteBuffer.allocate(capacity)
    val coderResult: CoderResult = encoder.reset.encode(charBuffer, byteBuffer, true)
    assert(coderResult == CoderResult.UNDERFLOW)
    coderResult match {
      case CoderResult.UNDERFLOW =>
        val byteArray: Array[Byte] = byteBuffer.array
        assert(byteArray sameElements utf8ByteArray3ForBufferTest)
      case CoderResult.OVERFLOW =>
        //Do nothing
    }
  }

  @Test
  def testBufferForDecoder1(): Unit = {
    val byteArray: Array[Byte] = utf8ByteArray2ForBufferTest
    val decoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder.
      onMalformedInput(CodingErrorAction.REPORT).
      onUnmappableCharacter(CodingErrorAction.REPORT)
    val byteBuffer: ByteBuffer = ByteBuffer.wrap(byteArray)
    val charBuffer: CharBuffer = decoder.decode(byteBuffer)
    val str = charBuffer.toString
    assert(str == "𠮷野家")
  }

  @Test
  def testBufferForDecoder2(): Unit = {
    val byteArray: Array[Byte] = utf8ByteArray2ForBufferTest
    val decoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder.
      onMalformedInput(CodingErrorAction.REPORT).
      onUnmappableCharacter(CodingErrorAction.REPORT)
    val byteBuffer: ByteBuffer = ByteBuffer.wrap(byteArray)
    val charBuffer: CharBuffer = CharBuffer.allocate(capacity)
    val coderResult: CoderResult = decoder.reset.decode(byteBuffer, charBuffer, true)
    assert(coderResult == CoderResult.UNDERFLOW)
    coderResult match {
      case CoderResult.UNDERFLOW =>
        val str = charBuffer.flip.toString
        assert(str == "𠮷野家")
      case CoderResult.OVERFLOW =>
        //Do nothing
    }
  }

  @Test
  def testFormat1(): Unit = {
    assert("%d%%".format(100) == "100%")
    assert(String.format("%d%%", 100.asInstanceOf[java.lang.Integer]) == "100%")
  }

  @Test
  def testFormat2(): Unit = {
    //%
    assert("%%".format() == "%")
    //Char
    assert("%c".format('x') == "x")
    //String
    assert("%s".format("xyz") == "xyz")
    //10進数
    assert("%d".format(123) == "123")
    assert("%d".format(-123) == "-123")
    //正数に+付き10進数
    assert("%+d".format(123) == "+123")
    assert("%+d".format(-123) == "-123")
    //正数にスペース付き10進数
    assert("% d".format(123) == " 123")
    assert("% d".format(-123) == "-123")
    //負数に()付き10進数
    assert("%(d".format(123) == "123")
    assert("%(d".format(-123) == "(123)")
    //3桁ごとカンマ付き10進数
    assert("%,d".format(12345) == "12,345")
    //0埋め
    assert("%05d".format(123) == "00123") //prepend
    assert(123.toString.padTo(5, '0') == "12300") //padToはappend
    //16進数
    assert("%x".format(123) == "7b")
    //16進数代替フォーム
    assert("%#x".format(123) == "0x7b")
    //8進数
    assert("%o".format(123) == "173")
    //8進数代替フォーム
    assert("%#o".format(123) == "0173")
    //右詰
    assert("[%4d]".format(123) == "[ 123]")
    //左詰
    assert("[%-4d]".format(123) == "[123 ]")
    //最大表示幅指定（幅を超えたものは切り捨て）
    assert("[%.4s]".format("xyzab") == "[xyza]")
    assert("[%5.4s]".format("xyzab") == "[ xyza]")
    assert("[%-5.4s]".format("xyzab") == "[xyza ]")
    //直前と同じ値（直前と同じものを引数に入れるくらいなら、これを使用した方が効率的）
    assert("%d:%<d:%d:%<d".format(1, 22) == "1:1:22:22")
    //引数のインデックス指定（同じものを連続せずに何度も引数に入れるくらいなら、これでまとめる方が効率的）
    assert("%d:%d:%d".format(1, 22, 333) == "1:22:333")
    assert("%1$d:%2$d:%3$d".format(1, 22, 333) == "1:22:333")
    assert("%3$d:%1$d:%2$d".format(1, 22, 333) == "333:1:22")
    assert("%3$d:%1$d:%d:%d:%3$d".format(1, 22, 333) == "333:1:1:22:333")
    //真偽値（小文字）
    //nullの場合はfalse
    //プリミティブ型booleanでもラッパークラスのBooleanでもない場合はtrue
    assert("%b".format(true) == "true")
    assert("%b".format(java.lang.Boolean.TRUE) == "true")
    assert("%b".format(0) == "true")
    assert("%b".format(false) == "false")
    assert("%b".format(java.lang.Boolean.FALSE) == "false")
    assert("%b".format(null) == "false")
    //真偽値（大文字）
    //nullの場合はfalse
    //プリミティブ型booleanでもラッパークラスのBooleanでもない場合はtrue
    assert("%B".format(true) == "TRUE")
    assert("%B".format(java.lang.Boolean.TRUE) == "TRUE")
    assert("%B".format(0) == "TRUE")
    assert("%B".format(false) == "FALSE")
    assert("%B".format(java.lang.Boolean.FALSE) == "FALSE")
    assert("%B".format(null) == "FALSE")
    //浮動小数
    assert("%e".format(math.Pi) == "3.141593e+00")
    assert("%f".format(math.Pi) == "3.141593")
    assert("%g".format(math.Pi) == "3.14159")
    assert("%a".format(math.Pi) == "0x1.921fb54442d18p1")
    //OS非依存の改行文字
    //Unix: \n
    //Windows: \r\n
    printf("%n")
    //日付・時刻
    printf("%1$tY年%1$tm月%1$td日%tA\n", new Date())
    printf("%1$tY年%1$tm月%1$td日%tA%n".formatLocal(java.util.Locale.US, new Date()))
    println("%1$tY年%1$tm月%1$td日%tA".formatLocal(java.util.Locale.JAPAN, new Date()))
    //ハッシュコード（16進数）
    printf("%h\n", new Object())
  }

  @Test
  def testDateTimeFormatter(): Unit = {
    val zonedDateTime: ZonedDateTime = ZonedDateTime.now
    val tdf: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    println(tdf.format(zonedDateTime))
    println(zonedDateTime.format(tdf))

    val localDate1: LocalDate = LocalDate.parse("2016/01/01", tdf)
    val date1: Date = Date.from(localDate1.atStartOfDay(ZoneOffset.UTC).toInstant)

    val ta: TemporalAccessor = tdf.parse("2016/01/01")
    val localDate2: LocalDate = LocalDate.from(ta)
    val date2: Date = Date.from(localDate2.atStartOfDay(ZoneOffset.of("+09:00")).toInstant)

    assert("%1$tY年%1$tm月%1$td日".format(date1) == "2016年01月01日")
    assert("%1$tY年%1$tm月%1$td日".format(date2) == "2016年01月01日")
  }
}
