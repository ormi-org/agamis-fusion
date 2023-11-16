package io.agamis.fusion.core.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.typed.Cluster
import akka.cluster.typed.Subscribe

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
