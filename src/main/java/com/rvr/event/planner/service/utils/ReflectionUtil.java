package com.rvr.event.planner.service.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static final String HANDLE_METHOD = "handle";
    public static final String APPLY_METHOD = "apply";

    public static <R> R invokeHandleMethod(Object target, Object param) {
        return invokeHandleMethod(target, param, HANDLE_METHOD);
    }

    public static <R> R invokeApplyMethod(Object target, Object param) {
        return invokeHandleMethod(target, param, APPLY_METHOD);
    }

    @SuppressWarnings("unchecked")
    private static <R> R invokeHandleMethod(Object target, Object param, String methodAction) {
        try {
            Method method = target.getClass().getMethod(methodAction, param.getClass());
            return (R) method.invoke(target, param);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}