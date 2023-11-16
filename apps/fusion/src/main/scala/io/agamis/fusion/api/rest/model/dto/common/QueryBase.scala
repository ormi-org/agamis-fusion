package io.agamis.fusion.api.rest.model.dto.common

import java.time.Instant

abstract class QueryBase {
    def offset: Option[Int]
    def limit: Option[Int]
    def createdAt: List[(String, Instant)]
    def updatedAt: List[(String, Instant)]
}
