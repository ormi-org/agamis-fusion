package io.agamis.fusion.core.actors.common

object CachePolicy extends Enumeration {
    type Value = String
    val ALWAYS: Value  = "always"
    val ON_READ: Value = "on-read"
    val NEVER: Value   = "never"

    def atLeast(policy: Value, least: Value): Boolean = {
        least match {
            case ALWAYS =>
                policy match {
                    case ALWAYS => true
                    case _      => false
                }
            case ON_READ =>
                policy match {
                    case ALWAYS  => true
                    case ON_READ => true
                    case _       => false
                }
            case NEVER     => true
            case _: String => throw new IllegalArgumentException()
        }
    }
}
