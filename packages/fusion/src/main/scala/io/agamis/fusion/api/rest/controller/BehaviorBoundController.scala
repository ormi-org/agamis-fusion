package io.agamis.fusion.api.rest.controller

import io.agamis.fusion.api.rest.model.dto.ModelDto
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse

/** @param C
  *   Response type from corresponding actor
  * @param AR
  *   Resulting ApiResponse
  * @param M
  *   Main model dto
  */
trait BehaviorBoundController[
    C <: Any,
    AR <: ApiResponse,
    M <: ModelDto
] {
    protected def mapToApiResponse(c: C): AR
    protected def excludeFields(user: M): M
}
