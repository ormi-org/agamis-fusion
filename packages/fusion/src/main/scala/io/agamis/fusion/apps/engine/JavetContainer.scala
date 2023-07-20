package io.agamis.fusion.apps.engine

import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.JavetEngineConfig
import com.caoccao.javet.interop.engine.JavetEnginePool
import io.agamis.fusion.apps.common.JavetLogger
import org.slf4j.Logger

import java.io.File
import com.caoccao.javet.interop.V8Host
import io.agamis.fusion.apps.common.enums.NodeReturnType._
import com.caoccao.javet.interop.executors.IV8Executor
import io.agamis.fusion.apps.common.enums
import java.util.concurrent.atomic.AtomicBoolean
import io.agamis.fusion.apps.engine.exceptions.InstantiateException

class JavetContainer(implicit _logger: Logger, config: JavetEngineConfig) {
    private var logger: JavetLogger = new JavetLogger()
    config.setJavetLogger(logger)
    private var _running: AtomicBoolean = new AtomicBoolean(false)
    private var _runtime: NodeRuntime = V8Host.getNodeInstance().createV8Runtime()

    def runtime: NodeRuntime = _runtime

    def isRunning: AtomicBoolean = _running

    def run[T](lambda: () => T): T = {
        return lambda()
    }
}

object JavetContainer {
    /**
      * Instantiate a new container running nodejs script meant to return provided type
      *
      * @param script the script file to run
      * @param returnType return type (default is VOID)
      * @param _logger a slf4j logger instance
      * @param config a JavetEngine configuration
      * @return container of a javet instance running provided script
      * @throws InstantiateException 
      */
    def ofScriptFile(script: File, returnType: NodeReturnType = VOID)(implicit _logger: Logger, config: JavetEngineConfig): JavetContainer = {
        val infoMsg = ">> JavetContainer#ofScriptFile(File, NodeReturnType) > Running NodeJS script from file:`%s`"
        val errMsg = "<< JavetContainer#ofScriptFile(File, NodeReturnType) > Failed running NodeJS script from file:`%s`"
        val scriptPath = script.getPath()
        val container = new JavetContainer()
        returnType match {
            case VOID => {
                container.run[Unit](() => {
                    _logger.info(String.format(infoMsg, scriptPath))
                    try {
                        container.runtime.getExecutor(script).executeVoid()
                    } catch {
                        case e: JavetException => {
                            _logger.error(String.format(errMsg, scriptPath), e)
                            throw InstantiateException(String.format("Failed instantiate `%s`", scriptPath), e)
                        }
                    }
                })
            }
        }
        return container
    }
}
