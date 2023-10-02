package io.agamis.fusion.api.rest.model.dto.profile

import io.agamis.fusion.api.rest.model.dto.common.QueryBase
import java.time.Instant

final case class ProfileQuery(
    id: List[String],
    alias: List[String],
    lastName: List[String],
    firstName: List[String],
    emails: List[String],
    lastLogin: List[(String, Instant)],
    limit: Option[Int],
    offset: Option[Int],
    createdAt: List[(String, Instant)],
    updatedAt: List[(String, Instant)],
    orderBy: List[(String, Int)],
    include: List[String]
) extends QueryBase

object ProfileQuery {
    def apply(
        param: (
            Option[List[String]],
            Option[List[String]],
            Option[List[String]],
            Option[List[String]],
            Option[List[String]],
            Option[List[(String, String)]],
            Option[Int],
            Option[Int],
            Option[List[(String, String)]],
            Option[List[(String, String)]],
            Option[List[(String, Int)]],
            Option[Iterable[String]],
        )
    ): ProfileQuery = {
        ProfileQuery(
          param._1.getOrElse(List()),
          param._2.getOrElse(List()),
          param._3.getOrElse(List()),
          param._4.getOrElse(List()),
          param._5.getOrElse(List()),
          param._6 match {
              case Some(l) => l.map(obj => (obj._1, Instant.parse(obj._2)))
              case None    => List()
          },
          param._7,
          param._8,
          param._9 match {
              case Some(l) => l.map(obj => (obj._1, Instant.parse(obj._2)))
              case None    => List()
          },
          param._10 match {
              case Some(l) => l.map(obj => (obj._1, Instant.parse(obj._2)))
              case None    => List()
          },
          param._11.getOrElse(List()),
          param._12.getOrElse(List()).toList
        )
    }
}
