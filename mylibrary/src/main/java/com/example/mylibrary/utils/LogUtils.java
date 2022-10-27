package com.example.mylibrary.utils;

import android.text.TextUtils;
import android.util.Log;

public class LogUtils {
    private LogUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }
    private static boolean logSwitch      = true;
    private static String tag = "TAG";

    public static void init(boolean logSwitch, String tag) {
        LogUtils.logSwitch = logSwitch;
        LogUtils.tag = tag;
    }

    public static void v(Object msg) {
        log(msg, null, Log.VERBOSE);
    }

    public static void v(Object msg, Throwable tr) {
        log(msg, tr, Log.VERBOSE);
    }

    public static void i(Object msg) {
        log(msg, null, Log.INFO);
    }

    public static void i(Object msg, Throwable tr) {
        log(msg, tr, Log.INFO);
    }

    public static void d(Object msg) {
        log(msg.toString(), null, Log.DEBUG);
    }

    public static void d(Object msg, Throwable tr) {
        log(msg.toString(), tr, Log.DEBUG);
    }

    public static void w(Object msg) {
        log(msg, null, Log.WARN);
    }

    public static void w(Object msg, Throwable tr) {
        log(msg, tr, Log.WARN);
    }

    public static void e(Object msg) {
        log(msg, null, Log.ERROR);
    }

    public static void e(Object msg, Throwable tr) {
        log(msg, tr, Log.ERROR);
    }

    private static void log(Object msg, Throwable tr, int type) {
        if (logSwitch) {
            switch (type){
                case Log.VERBOSE:
                    Log.v(generateTag(), String.valueOf(msg), tr);
                    break;
                case Log.INFO:
                    Log.i(generateTag(), String.valueOf(msg), tr);
                    break;
                case Log.DEBUG:
                    Log.d(generateTag(), String.valueOf(msg), tr);
                    break;
                case Log.WARN:
                    Log.w(generateTag(), String.valueOf(msg), tr);
                    break;
                case Log.ERROR:
                    Log.e(generateTag(), String.valueOf(msg), tr);
                    break;
                default:
                    break;
            }
        }
    }

    private static String generateTag() {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stacks[5];
        String format = tag + "::%s.%s:%d";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
//        return String.format(format, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return tag;
    }
}
