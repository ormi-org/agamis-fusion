package io.agamis.fusion.core.shard

import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.ClusterShardingSettings

trait ShardWithPersistence extends EntityShard {
    def init(
        settings: ClusterShardingSettings,
        wrapper: IgniteClientNodeWrapper
    )(implicit system: ActorSystem[_]): Unit =
        ???
}
