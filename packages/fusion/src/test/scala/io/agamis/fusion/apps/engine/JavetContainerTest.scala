package io.agamis.fusion.apps.engine

import org.scalatest.flatspec.AnyFlatSpec
import akka.event.slf4j.Logger
import org.slf4j
import scala.io.Source
import java.io.File
import com.caoccao.javet.interop.engine.JavetEngineConfig
import java.nio.file.Path
import java.nio.file.Paths

class JavetContainerTest extends AnyFlatSpec {

    final val helloWorldScriptFile = new File(getClass().getResource("../hello-world.js").getPath)

    "A JavetContainer" should "run nodejs program successfuly" in  {
        implicit val logger: slf4j.Logger = Logger.root
        implicit val config: JavetEngineConfig = new JavetEngineConfig
        val container = JavetContainer.ofScriptFile(helloWorldScriptFile)
    }
}
