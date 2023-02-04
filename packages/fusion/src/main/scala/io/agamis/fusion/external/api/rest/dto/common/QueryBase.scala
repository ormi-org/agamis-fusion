package io.agamis.fusion.external.api.rest.dto.common

import java.time.Instant

abstract class QueryBase (
  offset: Option[Int],
  limit: Option[Int],
  created_at: List[(String, Instant)],
  updated_at: List[(String, Instant)]
)