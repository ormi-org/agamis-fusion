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
import org.apache.ignite.IgniteException
import scala.util.Try
import org.slf4j.LoggerFactory

class IgniteClientNodeWrapper(system: ActorSystem[_]) extends Extension {
    
    private val cfg: IgniteConfiguration = new IgniteConfiguration()
    cfg.setClientMode(true)
    // doit aussi être activé dans les noeuds serveurs
    cfg.setPeerClassLoadingEnabled(true)
    cfg.setDeploymentMode(DeploymentMode.CONTINUOUS)

    private val ipFinder: TcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder()

    try {
        val nodeAddresses: java.util.List[String] = EnvContainer.getArray("fusion.core.db.ignite.nodes")
        ipFinder.setAddresses(nodeAddresses)
    } catch {
        case e: ConfigException => {
            throw new MissingIgniteConfException("fusion.core.db.ignite.nodes Config is missing", e)
        }
        case _: Throwable => throw new UnknownError("An unkown error occured while setting ignite cluster's nodes addresses")
    }

    cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder))

    private var _ignite: Ignite = null

    try {
        _ignite = Ignition.start(cfg)
    } catch {
        case e: IgniteException => {
            system.log.error("An error occured with ignite node" + e.getCause())
            println(e)
            system.terminate()
        }
        case _: Throwable => throw new UnknownError("An unkown error occured while starting ignite client node")
    }

    val log = LoggerFactory.getLogger("io.ogdt.fusion.fs")

    def ignite: Ignite = _ignite

    def close = ignite.close()
    
    // Let make a cache config manually
    def makeCacheConfig[K, V](): CacheConfiguration[K, V] = {
        new CacheConfiguration[K, V]
    }

    // Create a new cache from provided configuration
    def createCache[K, V](configuration: CacheConfiguration[K, V]): IgniteCache[K, V] = {
        ignite.createCache(configuration)
    }

    // Get a cache from ignite cluster to play with
    def getCache[K, V](cache: String, transactional: Boolean = false): IgniteCache[K, V] = {
        if (transactional) return ignite.cache(cache).withAllowAtomicOpsInTx()
        ignite.cache(cache)
    }

    def cacheExists(cache: String): Boolean = {
        log.info(ignite.cacheNames().toString())
        ignite.cacheNames().contains(cache)
    }
}

object IgniteClientNodeWrapper extends ExtensionId[IgniteClientNodeWrapper] {
    // will only be called once
    def createExtension(system: ActorSystem[_]): IgniteClientNodeWrapper = new IgniteClientNodeWrapper(system)

    // Java API
    def get(system: ActorSystem[_]): IgniteClientNodeWrapper = apply(system)
}
