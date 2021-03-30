package io.ogdt.fusion.core.db.ignite

import io.ogdt.fusion.env.EnvContainer
import io.ogdt.fusion.core.db.ignite.exceptions.MissingIgniteConfException

import com.typesafe.config.ConfigException

import akka.actor.typed.ActorSystem
import akka.actor.typed.Extension

import org.apache.ignite.{Ignite, Ignition}
import org.apache.ignite.configuration.{IgniteConfiguration, DeploymentMode}
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import akka.actor.typed.ExtensionId
import org.apache.ignite.IgniteCache
import org.apache.ignite.configuration.CacheConfiguration

class IgniteClientNodeWrapper(system: ActorSystem[_]) extends Extension {
    
    private val cfg: IgniteConfiguration = new IgniteConfiguration()
    cfg.setClientMode(true)
    // doit aussi être activé dans les noeuds serveurs
    cfg.setPeerClassLoadingEnabled(true)
    cfg.setDeploymentMode(DeploymentMode.CONTINUOUS)

    private val ipFinder: TcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder()

    try {
        val nodeAddresses: java.util.List[String] = EnvContainer.getArray("IGNITE_CLIENT_NODES_ADDR")
        ipFinder.setAddresses(nodeAddresses)
    } catch {
        case e: ConfigException => {
            throw new MissingIgniteConfException("IGNITE_CLIENT_NODES_ADDR Config is missing", e)
        }
        case _: Throwable => throw new UnknownError("An unkown error occured while setting ignite cluster's nodes addresses")
    }

    cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder))

    private val _ignite: Ignite = Ignition.start(cfg)

    def ignite: Ignite = _ignite
    
    // Let make a cache config manually
    def makeCacheConfig[K, V](): CacheConfiguration[K, V] = {
        new CacheConfiguration[K, V]
    }

    // Create a new cache from provided configuration
    def createCache[K, V](configuration: CacheConfiguration[K, V]): IgniteCache[K, V] = {
        ignite.createCache(configuration)
    }

    // Get a cache from ignite cluster to play with
    def getCache[K, V](cache: String ): IgniteCache[K, V] = {
        ignite.getOrCreateCache(cache)
    }
}

object IgniteClientNodeWrapper extends ExtensionId[IgniteClientNodeWrapper] {
    // will only be called once
    def createExtension(system: ActorSystem[_]): IgniteClientNodeWrapper = new IgniteClientNodeWrapper(system)

    // Java API
    def get(system: ActorSystem[_]): IgniteClientNodeWrapper = apply(system)
}
