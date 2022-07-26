package io.agamis.fusion.core.actors.data.entities.common

trait Pageable {
  def limit: Int
  def offset: Int
  def orderBy: List[(String, Int)]
}
