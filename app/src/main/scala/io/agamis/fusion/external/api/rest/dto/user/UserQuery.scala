package io.agamis.fusion.external.api.rest.dto.user

import io.agamis.fusion.external.api.rest.dto.common.QueryBase
import java.time.Instant

final case class UserQuery (
  id: List[String],
  username: List[String],
  limit: Int,
  offset: Int,
  createdAt: List[(String, Instant)],
  updatedAt: List[(String, Instant)],
  orderBy: List[(String, Int)]
) extends QueryBase(limit, offset, createdAt, updatedAt)

object UserQuery {
  def apply(param: (List[String], List[String], Int, Int, List[(String, String)], List[(String, String)], List[(String, Int)])): UserQuery = {
    UserQuery(
      param._1,
      param._2,
      param._3,
      param._4,
      param._5.map(obj => (obj._1, Instant.parse(obj._2))),
      param._6.map(obj => (obj._1, Instant.parse(obj._2))),
      param._7
    )
  }
}