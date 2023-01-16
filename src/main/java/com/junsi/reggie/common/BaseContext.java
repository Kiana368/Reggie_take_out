package com.junsi.reggie.common;

/**
 * 基于ThreadLocal 封装的工具类
 * 用于保存和取出当前用户id （线程局部变量）
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }
}

