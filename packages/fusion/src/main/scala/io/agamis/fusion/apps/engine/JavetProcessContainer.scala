package io.agamis.fusion.apps.engine

import org.slf4j.Logger
import com.caoccao.javet.interop.engine.JavetEngineConfig

class JavetProcessContainer(implicit _logger: Logger, config: JavetEngineConfig) extends JavetContainer {
    def run(thread: Thread): Unit = {
        thread.start()
    }
        
    def close(): Unit = {
        runtime.setPurgeEventLoopBeforeClose(true)
        runtime.close()
    }
}
