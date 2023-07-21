package io.agamis.fusion.apps.common

import org.slf4j.Logger
import com.caoccao.javet.interfaces.IJavetLogger
import com.caoccao.javet.annotations.V8Function

class JavetLogger(implicit val logger: Logger) extends IJavetLogger {

    @V8Function
    override def debug(message: String): Unit = {
        if (logger.isDebugEnabled()) {
            logger.debug(message)
        }
    }

    @V8Function
    override def error(message: String): Unit = {
        if (logger.isErrorEnabled()) {
            logger.error(message)
        }
    }

    @V8Function
    override def error(message: String, cause: Throwable): Unit = {
        if (logger.isErrorEnabled()) {
            logger.error(message, cause)
        }
    }

    @V8Function
    override def info(message: String): Unit = {
        if (logger.isInfoEnabled()) {
            logger.info(message)
        }
    }

    @V8Function
    override def warn(message: String): Unit = {
        if (logger.isWarnEnabled()) {
            logger.warn(message)
        }
    }
}
