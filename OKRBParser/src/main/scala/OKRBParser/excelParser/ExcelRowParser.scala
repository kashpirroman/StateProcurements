package OKRBParser.excelParser
import org.apache.poi.ss.usermodel.{Cell, Row}
trait ExcelRowParser {
  import cats.syntax.either._
  def parseValue[A](parse:Cell=>A)(numberOfCell:Int)(row: Row): FastExcelParseResult[A] = {
    Either.catchNonFatal {
      val cell = row.getCell(numberOfCell)
      parse(cell)
    }.leftMap(_ => List(s"невозможно распарсить ячейку   $numberOfCell в строке ${row.getRowNum}"))
  }
  def nonNegativeValue(numberOfCell:Int)(row:Int)(value:Int):FastExcelParseResult[Int]={
  value.asRight[ErrorList].ensure(List(s"отрицательное значение $value в ячейке $numberOfCell,строке $row"))(_>0)
  }
  def parseIntValue(numberOfCell:Int)(row: Row): FastExcelParseResult[Int] ={
    parseValue(cell=>{cell.getStringCellValue.toInt})(numberOfCell)(row).
      flatMap(nonNegativeValue(numberOfCell)(row.getRowNum))
  }
  def parseStringValue: Int => Row => FastExcelParseResult[String] ={
    parseValue(cell=>cell.getStringCellValue)
  }
}