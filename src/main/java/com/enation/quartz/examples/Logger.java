package com.enation.quartz.examples;

/**
 * Logger
 *
 * @author panjn
 * @date 2016/10/3
 */
public class Logger {

    public void info(String msg) {
        System.out.println(msg);
    }

    public void warn(String warnMsg) {
        System.out.println("警告:" + warnMsg);
    }

    public void error(String errorMsg, Error e) {
        System.out.println("错误：" + errorMsg);
        e.printStackTrace();
    }
}
