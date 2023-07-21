package io.agamis.fusion.apps.engine

import org.slf4j.Logger
import com.caoccao.javet.interop.engine.JavetEngineConfig

protected class JavetProcessContainer(implicit _logger: Logger, config: JavetEngineConfig) extends JavetContainer {
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
