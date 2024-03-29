package io.agamis.fusion.api.rest.model.dto.user

import io.agamis.fusion.api.rest.model.dto.common.QueryBase
import java.time.Instant

final case class UserQuery(
    id: List[String],
    username: List[String],
    limit: Option[Int],
    offset: Option[Int],
    createdAt: List[(String, Instant)],
    updatedAt: List[(String, Instant)],
    orderBy: List[(String, Int)],
    include: List[String]
) extends QueryBase

object UserQuery {
    def apply(
        param: (
            Option[List[String]],
            Option[List[String]],
            Option[Int],
            Option[Int],
            Option[List[(String, String)]],
            Option[List[(String, String)]],
            Option[List[(String, Int)]],
            Option[Iterable[String]],
        )
    ): UserQuery = {
        UserQuery(
          param._1.getOrElse(List()),
          param._2.getOrElse(List()),
          param._3,
          param._4,
          param._5 match {
              case Some(l) => l.map(obj => (obj._1, Instant.parse(obj._2)))
              case None    => List()
          },
          param._6 match {
              case Some(l) => l.map(obj => (obj._1, Instant.parse(obj._2)))
              case None    => List()
          },
          param._7.getOrElse(List()),
          param._8.getOrElse(List()).toList
        )
    }
}
