package io.agamis.fusion.env

import com.typesafe.config.{ConfigFactory, Config}

object EnvContainer {

    val config: Config = ConfigFactory.load()

    def getString(key: String): String = {
        config.getString(key)
    }

    def getArray(key: String): java.util.List[String] = {
        config.getStringList(key)
    }
}

