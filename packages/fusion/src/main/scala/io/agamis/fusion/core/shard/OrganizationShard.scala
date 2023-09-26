package io.agamis.fusion.core.shard

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityRef
import io.agamis.fusion.core.actor.entity.Organization
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

object OrganizationShard {

    def init(implicit system: ActorSystem[_]): Unit = {
        ClusterSharding(system).init(
          Entity(typeKey = Organization.TypeKey) { entityCtx =>
              implicit val wrapper: IgniteClientNodeWrapper =
                  IgniteClientNodeWrapper(system)
              Organization(entityCtx.entityId)
          }
        )
    }

    def ref(
        orgId: String
    )(implicit system: ActorSystem[_]): EntityRef[Organization.Command] = {
        ClusterSharding(system).entityRefFor(Organization.TypeKey, orgId)
    }
}
