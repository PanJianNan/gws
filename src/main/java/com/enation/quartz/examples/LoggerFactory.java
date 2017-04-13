package com.enation.quartz.examples;

/**
 * LoggerFactory
 *
 * @author panjn
 * @date 2016/10/3
 */
public class LoggerFactory {

    private static Logger logger = null;

    public static Logger getLogger(Class<?> clazz) {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }
}
