package io.agamis.fusion.apps.actors.enums

sealed trait ContainerType
case object SEQUENTIAL_SCRIPT extends ContainerType
case object PROCESS           extends ContainerType
