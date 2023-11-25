package io.agamis.fusion.core.shard

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityRef

trait EntityShard {
    def init(implicit system: ActorSystem[_]): Unit
    def ref(id: String)(implicit
        system: ActorSystem[_]
    ): EntityRef[_]
}
