package io.agamis.fusion.api.rest.controller

import io.agamis.fusion.api.rest.model.dto.ModelDto
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse
import io.agamis.fusion.core.actors.data.entities.common.DataActorResponse

/** @param R
  *   Response type from corresponding actor
  * @param AR
  *   Resulting ApiResponse
  * @param M
  *   Main model dto
  */
trait BehaviorBoundController[
    R <: DataActorResponse,
    AR <: ApiResponse,
    M <: ModelDto
] {
    protected def mapToApiResponse(r: R): AR
    protected def excludeFields(user: M): M
}
