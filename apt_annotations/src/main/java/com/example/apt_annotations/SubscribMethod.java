package com.example.apt_annotations;

import java.lang.reflect.Method;

public class SubscribMethod {

    private Method method;

    private ThreadMode threadMode;

    private Class<?> type;

    public SubscribMethod(Method method, Class<?> type, ThreadMode threadMode) {
        this.method = method;
        this.threadMode = threadMode;
        this.type = type;
    }

    public SubscribMethod(Class<?> clazz, String name, Class<?> type, ThreadMode threadMode) {
        this.threadMode = threadMode;
        this.type = type;
        try {
            this.method = clazz.getDeclaredMethod(name, type);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public Class<?> getEventType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}

