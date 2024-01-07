package io.agamis.fusion.core.shard

import io.agamis.fusion.core.actor.entity.Organization
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.ClusterShardingSettings
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.scaladsl.Entity
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityRef

class OrganizationShard extends ShardWithPersistence {

    override def init(
        settings: ClusterShardingSettings,
        wrapper: IgniteClientNodeWrapper
    )(implicit
        system: ActorSystem[_]
    ): Unit = {
        implicit val finalWrapper: IgniteClientNodeWrapper = wrapper
        ClusterSharding(system).init(
          Entity(typeKey = Organization.TypeKey) { entityCtx =>
              Organization(entityCtx.shard, entityCtx.entityId)
          } withSettings (settings)
        )
    }

    def ref(
        id: String
    )(implicit system: ActorSystem[_]): EntityRef[Organization.Command] = {
        ClusterSharding(system).entityRefFor(Organization.TypeKey, id)
    }
}

object OrganizationShard extends OrganizationShard
