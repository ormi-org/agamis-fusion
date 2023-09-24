package io.agamis.fusion.api.rest.controller

import io.agamis.fusion.api.rest.model.dto.common.ModelDto
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse

/** @param C
  *   Response type from corresponding actor
  * @param M
  *   Main model dto
  */
trait BehaviorBoundController[
    C <: Any, // TODO: update type
    M <: ModelDto
] {
    protected def mapToApiResponse(c: C): ApiResponse
    protected def excludeFields(m: M): M
}
