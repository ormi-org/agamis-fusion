package io.agamis.fusion.apps.common

import org.slf4j.Logger
import com.caoccao.javet.interfaces.IJavetLogger

class JavetLogger(implicit val logger: Logger) extends IJavetLogger {

    override def debug(message: String): Unit = {
        if (logger.isDebugEnabled()) {
            logger.debug(message)
        }
    }
    override def error(message: String): Unit = {
        if (logger.isErrorEnabled()) {
            logger.error(message)
        }
    }

    override def error(message: String, cause: Throwable): Unit = {
        if (logger.isErrorEnabled()) {
            logger.error(message, cause)
        }
    }

    override def info(message: String): Unit = {
        if (logger.isInfoEnabled()) {
            logger.info(message)
        }
    }

    override def warn(message: String): Unit = {
        if (logger.isWarnEnabled()) {
            logger.warn(message)
        }
    }
}
