package io.agamis.fusion.core.actors.data.entities.common

trait Pageable {
  def limit: Long
  def offset: Long
  def orderBy: List[(String, Int)]
}
