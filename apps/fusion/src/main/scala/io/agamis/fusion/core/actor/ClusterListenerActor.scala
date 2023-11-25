package io.agamis.fusion.core.actor

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.cluster.ClusterEvent.ClusterDomainEvent
import org.apache.pekko.cluster.ClusterEvent.MemberRemoved
import org.apache.pekko.cluster.ClusterEvent.MemberUp
import org.apache.pekko.cluster.ClusterEvent.UnreachableMember
import org.apache.pekko.cluster.typed.Cluster
import org.apache.pekko.cluster.typed.Subscribe

object ClusterListenerActor {
    def apply(): Behavior[ClusterDomainEvent] =
        Behaviors.setup[ClusterDomainEvent] { context =>
            val cluster = Cluster(context.system)
            cluster.subscriptions ! Subscribe(
              context.self.ref,
              classOf[ClusterDomainEvent]
            )

            context.log.info(
              s"started actor ${context.self.path} - (${context.self.getClass})"
            )

            def running(): Behavior[ClusterDomainEvent] =
                Behaviors.receive { (context, message) =>
                    message match {
                        case MemberUp(member) =>
                            context.log.info("Member is Up: {}", member.address)
                            Behaviors.same
                        case UnreachableMember(member) =>
                            context.log.info(
                              "Member detected as unreachable: {}",
                              member
                            )
                            Behaviors.same
                        case MemberRemoved(member, previousState) =>
                            context.log.info(
                              "Member is Removed: {} after {}",
                              member.address,
                              previousState
                            )
                            Behaviors.same
                        case _ =>
                            context.log.warn("Received unhandled Cluster Event")
                            Behaviors.same
                    }
                }
            running()
        }
}
