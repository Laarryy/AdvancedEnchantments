package me.egg82.ae;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExternalAPI {
    private static ExternalAPI api = null;

    private final Object concrete;
    private final Class<?> concreteClass;
    private final Class<?> exceptionClass;

    private final ConcurrentMap<String, Method> methodCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Method> exceptionMethodCache = new ConcurrentHashMap<>();

    private ExternalAPI(URLClassLoader classLoader) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader cannot be null.");
        }

        try {
            concreteClass = classLoader.loadClass("me.egg82.ae.EnchantAPI");
            Constructor<?> constructor = concreteClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            concrete = constructor.newInstance();
            exceptionClass = classLoader.loadClass("me.egg82.ae.APIException");
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Could not get EnchantAPI from classLoader.", ex);
        }
    }

    public static ExternalAPI getInstance() { return api; }

    public static void setInstance(URLClassLoader classLoader) {
        if (api != null) {
            throw new IllegalStateException("api is already set.");
        }
        api = new ExternalAPI(classLoader);
    }

    private Object invokeMethod(String name, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method tmp = methodCache.get(name);
        if (tmp == null) {
            synchronized (this) {
                tmp = methodCache.get(name);
                if (tmp == null) {
                    tmp = concreteClass.getMethod(name, getParamClasses(params));
                    methodCache.put(name, tmp);
                }
            }
        }

        return tmp.invoke(concrete, params);
    }

    private Object invokeExceptionMethod(String name, Throwable ex, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method tmp = exceptionMethodCache.get(name);
        if (tmp == null) {
            synchronized (this) {
                tmp = exceptionMethodCache.get(name);
                if (tmp == null) {
                    tmp = exceptionClass.getMethod(name, getParamClasses(params));
                    exceptionMethodCache.put(name, tmp);
                }
            }
        }

        return tmp.invoke(ex, params);
    }

    private Class[] getParamClasses(Object[] params) {
        Class[] retVal = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            retVal[i] = (params[i] != null) ? params[i].getClass() : null;
        }
        return retVal;
    }

    private APIException convertToAPIException(Throwable e) throws APIException {
        try {
            boolean hard = (Boolean) invokeExceptionMethod("isHard", e);
            String message = (String) invokeExceptionMethod("getMessage", e);
            Throwable cause = (Throwable) invokeExceptionMethod("getCause", e);
            return new APIException(hard, message, cause);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not convert exception.", ex);
        }
    }
}
