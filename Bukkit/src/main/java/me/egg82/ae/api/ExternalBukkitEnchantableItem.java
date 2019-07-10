package me.egg82.ae.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.egg82.ae.APIException;
import org.bukkit.inventory.ItemStack;

public class ExternalBukkitEnchantableItem {
    private static URLClassLoader classLoader = null;

    private final Object concrete;
    private static Class<?> concreteClass;
    private static Class<?> exceptionClass;

    private static final ConcurrentMap<String, Method> staticMethodCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Method> exceptionMethodCache = new ConcurrentHashMap<>();

    private static final Object staticMethodCacheLock = new Object();
    private static final Object methodCacheLock = new Object();
    private static final Object exceptionMethodCacheLock = new Object();

    private ExternalBukkitEnchantableItem(Object concrete) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader cannot be null.");
        }

        this.concrete = concrete;
    }

    public static URLClassLoader getClassLoader() { return classLoader; }

    public static void setClassLoader(URLClassLoader classLoader) {
        if (ExternalBukkitEnchantableItem.classLoader != null) {
            throw new IllegalStateException("classLoader is already set.");
        }

        ExternalBukkitEnchantableItem.classLoader = classLoader;

        try {
            concreteClass = classLoader.loadClass("me.egg82.ae.api.BukkitEnchantableItem");
            exceptionClass = classLoader.loadClass("me.egg82.ae.APIException");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not get EnchantAPI from classLoader.", ex);
        }
    }

    public static ExternalBukkitEnchantableItem fromItemStack(ItemStack item) throws APIException {
        try {
            return new ExternalBukkitEnchantableItem(invokeStaticMethod("fromItemStack", item));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void rewriteMeta() throws APIException {
        try {
            invokeMethod(concrete, "rewriteMeta");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public final Object getConcrete() throws APIException {
        try {
            return invokeMethod(concrete, "getConcrete");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public Set<GenericEnchantmentTarget> getEnchantmentTargets() throws APIException {
        try {
            return (Set<GenericEnchantmentTarget>) invokeMethod(concrete, "getEnchantmentTargets");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public boolean hasEnchantment(GenericEnchantment enchantment) throws APIException {
        try {
            return (Boolean) invokeMethod(concrete, "hasEnchantment", enchantment);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public int getEnchantmentLevel(GenericEnchantment enchantment) throws APIException {
        try {
            return (Integer) invokeMethod(concrete, "getEnchantmentLevel", enchantment);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void setEnchantmentLevel(GenericEnchantment enchantment, int level) throws APIException {
        try {
            invokeMethod(concrete, "setEnchantmentLevel", enchantment, level);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void setEnchantmentLevels(Map<GenericEnchantment, Integer> enchantments) throws APIException {
        try {
            invokeMethod(concrete, "setEnchantmentLevels", enchantments);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void addEnchantment(GenericEnchantment enchantment) throws APIException {
        try {
            invokeMethod(concrete, "addEnchantment", enchantment);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void addEnchantments(Collection<GenericEnchantment> enchantments) throws APIException {
        try {
            invokeMethod(concrete, "addEnchantments", enchantments);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void removeEnchantment(GenericEnchantment enchantment) throws APIException {
        try {
            invokeMethod(concrete, "removeEnchantment", enchantment);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public void removeEnchantments(Collection<GenericEnchantment> enchantments) throws APIException {
        try {
            invokeMethod(concrete, "removeEnchantments", enchantments);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public Map<GenericEnchantment, Integer> getEnchantments() throws APIException {
        try {
            return (Map<GenericEnchantment, Integer>) invokeMethod(concrete, "getEnchantments");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new APIException(true, "Could not invoke base method.", ex);
        }
    }

    public boolean equals(Object o) {
        try {
            return (Boolean) invokeMethod(concrete, "equals", o);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Could not invoke base method.", ex);
        }
    }

    public int hashCode() {
        try {
            return (Integer) invokeMethod(concrete, "hashCode");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Could not invoke base method.", ex);
        }
    }

    private static Object invokeStaticMethod(String name, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method tmp = staticMethodCache.get(name);
        if (tmp == null) {
            synchronized (staticMethodCacheLock) {
                tmp = staticMethodCache.get(name);
                if (tmp == null) {
                    tmp = concreteClass.getMethod(name, getParamClasses(params));
                    staticMethodCache.put(name, tmp);
                }
            }
        }

        return tmp.invoke(null, params);
    }

    private static Object invokeMethod(Object concrete, String name, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method tmp = methodCache.get(name);
        if (tmp == null) {
            synchronized (methodCacheLock) {
                tmp = methodCache.get(name);
                if (tmp == null) {
                    tmp = concreteClass.getMethod(name, getParamClasses(params));
                    methodCache.put(name, tmp);
                }
            }
        }

        return tmp.invoke(concrete, params);
    }

    private static Object invokeExceptionMethod(String name, Throwable ex, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method tmp = exceptionMethodCache.get(name);
        if (tmp == null) {
            synchronized (exceptionMethodCacheLock) {
                tmp = exceptionMethodCache.get(name);
                if (tmp == null) {
                    tmp = exceptionClass.getMethod(name, getParamClasses(params));
                    exceptionMethodCache.put(name, tmp);
                }
            }
        }

        return tmp.invoke(ex, params);
    }

    private static Class[] getParamClasses(Object[] params) {
        Class[] retVal = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            retVal[i] = (params[i] != null) ? params[i].getClass() : null;
        }
        return retVal;
    }

    private static APIException convertToAPIException(Throwable e) throws APIException {
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
