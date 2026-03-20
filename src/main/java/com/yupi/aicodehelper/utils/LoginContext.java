package com.yupi.aicodehelper.utils;

public class LoginContext {

    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();

    public static void set(Long userId) {
        currentUser.set(userId);
    }

    public static Long get() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}