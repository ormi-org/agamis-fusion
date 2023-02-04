package io.agamis.fusion.core.actors.data.entities.common

import java.time.Instant

trait Timetracked {
  def createdAt: List[(String, Instant)]
  def updatedAt: List[(String, Instant)]
}
