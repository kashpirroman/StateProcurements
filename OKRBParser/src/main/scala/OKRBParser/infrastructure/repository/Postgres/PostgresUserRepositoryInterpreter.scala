package OKRBParser.infrastructure.repository.Postgres

import OKRBParser.domain.position._
import cats.effect.Sync
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.{Read, fragment}

class PostgresUserRepositoryInterpreter[F[_] : Sync](tx: Transactor[F],
                                                     maxThreadPool: Int = 1) extends UserRepositoryAlgebra[F] {

  import UserSQL._

  override def getUser(id: UserId): F[Option[User]] = selectById(id).option.transact(tx)

  override def checkPassword(usernamePasswordCredentials: UsernamePasswordCredentials): F[Option[User]] = {
    getByUCredentials(usernamePasswordCredentials).option.transact(tx)
  }

 // override def maxThreadPool(): Int = 1

  override def getUserList: F[List[User]] = selectUsers.to[List].transact(tx)
}

object UserSQL {
  implicit val userReader: Read[User] = Read[(Int,
    Int, String, String,
    String, String, String,
    String, String, String,
    String)].map(u => User(
    Position(
      u._9, Role.fromRepr(u._11).getOrElse(Role.Unauthorized),
      u._10, u._1.some),
    u._3, u._4, u._5,
    u._6, u._7, u._8,
    u._2.some))


  private def selectUserFr: fragment.Fragment =
    fr"""select * from users
        |natural join militaryposition
        |where""".stripMargin

  def selectUsers: doobie.Query0[User] = (selectUserFr ++ fr""" role= 'User' """).query[User]

  def selectById(id: UserId): doobie.Query0[User] = (selectUserFr ++ fr"""  userid=$id""").query[User]

  def getByUCredentials(userCrd: UsernamePasswordCredentials): doobie.Query0[User] = {
    (selectUserFr ++
      fr"""  useremail=${userCrd.email} and userpassword=${userCrd.password}""").
      query[User]
  }
}
