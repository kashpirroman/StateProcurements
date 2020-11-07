package OKRBParser.infrastructure.endpoints

import OKRBParser.ParseError
import OKRBParser.domain.auth.AuthService
import OKRBParser.domain.parseExcel.okrb.{OKRBProduct, OKRBService}
import OKRBParser.domain.purchase.PurchaseService
import scala.concurrent.duration._
import cats.Monad
import cats.effect.ConcurrentEffect
import cats.implicits._
import org.http4s.EntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Part
import org.http4s.server.middleware.{CORS, CORSConfig}
import org.http4s.{EntityDecoder, Header, Headers, HttpRoutes, ApiVersion => _}
class OKRBEndpoints[F[_] : ConcurrentEffect : Monad](service: OKRBService[F]) extends Http4sDsl[F] {
  lazy implicit val okrbListEncoder: EntityDecoder[F, List[OKRBProduct]] = jsonOf[F, List[OKRBProduct]]

  private def okrbParseEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req@POST -> Root / "okrb" => req.decodeWith(multipart[F], strict = true) {
        request =>
          request.parts.find(filterFileTypes) match {
            case Some(value) => Ok(service.insertOKRB(value).compile.toList.map(_.toString()))
            case None => Ok("не эксель файл")
          }

      }.handleErrorWith {
        case ParseError(list) => BadRequest(list.toString())
      }
    }

  private def getOkrbList: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "okrb" => service.getOKRB().flatMap(list => Ok(list))

  }

  def filterFileTypes(part: Part[F]): Boolean =
    part.headers.toList.exists(_.value.contains(s".xls"))

  private val methodConfig = CORSConfig(
    anyOrigin = true,
    anyMethod = false,
    allowedMethods = Some(Set("GET", "POST")),
    allowCredentials = true,
    maxAge = 1.day.toSeconds)
  def endpoints(): HttpRoutes[F] = CORS(okrbParseEndpoint<+>getOkrbList,methodConfig)

}
object OKRBEndpoints{
  def endpoints[F[_] : ConcurrentEffect : Monad](service: OKRBService[F],
                                                 auth: AuthService[F]): HttpRoutes[F] = {
    new OKRBEndpoints[F](service).endpoints().map(_.withHeaders(Headers.of(
      Header("Access-Control-Allow-Origin", "http://localhost:4200"),
      Header("Access-Control-Allow-Credentials", "true"))))
  }
}