package io.ogdt.fusion.env

import scala.util.Properties
import com.typesafe.config.{ConfigFactory, Config}

object EnvContainer {

    val config: Config = ConfigFactory.load()

    def getString(key: String): String = {
        return config.getString(key)
    }

    def getArray(key: String): java.util.List[String] = {
        return config.getStringList(key)
    }
}

