package OKRBParser.infrastructure.repository.MySql

import OKRBParser.domain.okrb.{OKRBProduct, OKRBRepositoryAlgebra}
import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import fs2.Chunk
class MySqlOKRBRepositoryInterpreter[F[_]:Sync](tx:Transactor[F],
                                                maxThreadPool:Int)
  extends OKRBRepositoryAlgebra[F]{

  def getOKRBList(): F[List[OKRBProduct]] = {
    sql"""Select * from okrb"""
      .query[OKRBProduct]
      .to[List]
      .transact(tx)
  }

  override def insertOKRBChunk(dataChunk: Chunk[OKRBProduct]): F[Int] = {
    val sqlUpdate=
      """insert into okrb
           |(section, class, subcategories, groupings, name)
           |values (?,?,?,?,?)""".stripMargin
    Update[OKRBProduct](sqlUpdate)
      .updateMany(dataChunk)
      .transact(tx)
  }

  override def clearOKRBList(): F[Int] = {
    sql"""DELETE from okrb where true""".update.run.transact(tx)
  }

  //override def maxThreadPool(): Int = maxThreadPool

  override def getOKRBList(pageSize: Int, page: Int, searchField: String): F[List[OKRBProduct]] = ???

  override def getLength(str: String): F[Option[Int]] = ???
}

object MySqlOKRBRepositoryInterpreter {

}
