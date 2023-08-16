package io.agamis.fusion.apps.engine

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import ch.qos.logback.classic
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.engine.JavetEngineConfig
import org.scalatest.BeforeAndAfter
import org.scalatest.exceptions.TestCanceledException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import scala.io.Source
import scala.language.postfixOps

import sys.process._
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpResponse
import akka.util.ByteString
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class JavetContainerTest extends AnyWordSpec {

    "A JavetContainer" when {
        val helloWorldScriptFile = new File(getClass().getResource("./hello-world.js").getPath)
        "run single file nodejs program" should {
            "succeed" in {
                implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)
                val listAppender: ListAppender[ILoggingEvent] = new ListAppender()
                logger match {
                    case logback: classic.Logger => logback.addAppender(listAppender)
                    case _ => fail("Logger is not a logback logger")
                }
                listAppender.start()
                implicit val config: JavetEngineConfig = new JavetEngineConfig
                config.setJSRuntimeType(JSRuntimeType.Node)
                val container = JavetContainer.ofScriptFile(helloWorldScriptFile)

                val expectedMessage = "Hello, world"

                val logsList = listAppender.list
                assert(logsList.stream().filter((log) => {
                    log.getLevel().equals(Level.INFO) && log.getMessage().equals(expectedMessage)
                }).findFirst().isPresent())
            }
        }
    }
    
    "A JavetContainer" when {
        val resourcePath = getClass().getResource("").getPath
        "run single file nodejs w/modules bundled program" should {
            "succeed" in {
                // run node scripts for bundling
                Process("npm i", new File(resourcePath + "hello-world.express")) !;
                Process("npx webpack", new File(resourcePath + "hello-world.express")) !;
                val helloWorldExpressBundle = new File(getClass().getResource("./hello-world.express/dist/app.bundle.js").getPath)
                implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)
                val listAppender: ListAppender[ILoggingEvent] = new ListAppender()
                logger match {
                    case logback: classic.Logger => logback.addAppender(listAppender)
                    case _ => fail("Logger is not a logback logger")
                }
                listAppender.start()
                implicit val config: JavetEngineConfig = new JavetEngineConfig
                config.setJSRuntimeType(JSRuntimeType.Node)
                config.setAllowEval(true)
                val container = JavetContainer.ofProcessFile(helloWorldExpressBundle)
                var retry = 0;
                var success = false;
                implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
                implicit val ec = system.executionContext
                val http = Http()
                while (!success && container.isRunning.get() && retry < 3) {
                    TimeUnit.MILLISECONDS.sleep(500);
                    val req = RequestBuilding.Get("http://localhost:3000")
                    val res = Await.ready(http.singleRequest(req), Duration.Inf).value.get
                    res match {
                        case Success(res) =>
                            res match {
                                case HttpResponse(StatusCodes.OK, headers, entity, _) => {
                                    success = true
                                    entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
                                        logger.info(body.utf8String)
                                        assert(body.utf8String.equals("Hello World!"))
                                    }
                                }
                                case _ => fail()
                            }
                        case Failure(e) => retry = retry + 1
                    }
                }
            }
        }
    }
}
