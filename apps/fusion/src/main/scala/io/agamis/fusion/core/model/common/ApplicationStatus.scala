package io.agamis.fusion.core.model.common

sealed abstract class ApplicationStatus(val index: Int)
object ApplicationStatus {
    final case object NOT_INSTALLED extends ApplicationStatus(index = 0)
    final case object INSTALLED     extends ApplicationStatus(index = 1)
}
