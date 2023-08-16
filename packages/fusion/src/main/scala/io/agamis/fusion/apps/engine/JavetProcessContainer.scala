package io.agamis.fusion.apps.engine

import com.caoccao.javet.interop.engine.JavetEngineConfig
import org.slf4j.Logger

/** 
  * A modified JavetContainer taking care of NodeJs Application lifecycle inside Javet V8 engine
  * This modified container can run long-running processes
  *
  * @param _logger the logger the container should use
  * @param config the config to use for Javet engine
  */
class JavetProcessContainer protected[engine](implicit _logger: Logger, config: JavetEngineConfig) extends JavetContainer {
    def run(lambda: () => Unit): Unit = {
        val thread = new Thread(() => {
            try {
                lambda()
            } catch {
                case e: InterruptedException => {
                    _logger.error("<< JavetProcessContainer#run(() => Unit) > Thread for running app interupted", e)
                    throw e
                }
            }
        })
        thread.start()
    }
        
    def close(): Unit = {
        runtime.setPurgeEventLoopBeforeClose(true)
        runtime.close()
    }
}
