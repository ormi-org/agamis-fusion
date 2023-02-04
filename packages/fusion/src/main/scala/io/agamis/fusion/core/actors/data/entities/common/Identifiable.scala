package io.agamis.fusion.core.actors.data.entities.common

import java.util.UUID

trait Identifiable {
  def id: List[UUID]
}