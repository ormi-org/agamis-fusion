package io.agamis.fusion.core.actors.data.entities.common

trait Pageable {
  def limit: Option[Int]
  def offset: Option[Int]
  def orderBy: List[(String, Int)]
}
