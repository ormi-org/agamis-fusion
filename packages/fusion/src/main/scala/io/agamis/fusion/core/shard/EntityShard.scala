package io.agamis.fusion.core.shard

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.EntityRef

trait EntityShard {
    def init(implicit system: ActorSystem[_]): Unit
    def ref(id: String)(implicit
        system: ActorSystem[_]
    ): EntityRef[_]
}
