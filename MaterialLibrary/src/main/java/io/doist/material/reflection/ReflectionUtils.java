package io.doist.material.reflection;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {
    private static final String LOG_TAG = ReflectionUtils.class.getSimpleName();

    public static final Class<?>[] EMPTY_TYPES = new Class<?>[0];
    public static final Object[] EMPTY_PARAMETERS = new Object[0];

    private static final Map<String, Class<?>> sClassCache = new ConcurrentHashMap<>(8);
    private static final Map<Class<?>, Map<String, Method>> sClassMethodCache = new ConcurrentHashMap<>(8);
    private static final Map<Class<?>, Map<String, Field>> sClassFieldCache = new ConcurrentHashMap<>(8);

    private ReflectionUtils() {
        throw new InstantiationError("Must not instantiate this class");
    }

    private static Map<String, Method> ensureMethodCache(Class<?> clazz) {
        Map<String, Method> methodCache = sClassMethodCache.get(clazz);
        if (methodCache == null) {
            methodCache = new ConcurrentHashMap<>(4);
            sClassMethodCache.put(clazz, methodCache);
        }
        return methodCache;
    }

    private static Map<String, Field> ensureFieldCache(Class<?> clazz) {
        Map<String, Field> fieldCache = sClassFieldCache.get(clazz);
        if (fieldCache == null) {
            fieldCache = new ConcurrentHashMap<>(4);
            sClassFieldCache.put(clazz, fieldCache);
        }
        return fieldCache;
    }

    public static Class<?> getClass(String className) {
        Class<?> clazz = sClassCache.get(className);
        if (clazz == null) {
            try {
                clazz = Class.forName(className);
                sClassCache.put(className, clazz);
            } catch (ClassNotFoundException e) {
                Log.w(LOG_TAG, e);
            }
        }
        return clazz;
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        if (clazz != null) {
            final Map<String, Method> methodCache = ensureMethodCache(clazz);
            method = methodCache.get(methodName);
            if (method == null) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    method.setAccessible(true);
                    // Cache method.
                    methodCache.put(methodName, method);
                } catch (NoSuchMethodException e) {
                    Log.w(LOG_TAG, e);
                }
            }
        }
        return method;
    }

    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        Field field = null;
        if (clazz != null) {
            final Map<String, Field> fieldCache = ensureFieldCache(clazz);
            field = fieldCache.get(fieldName);
            if (field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    // Cache field.
                    fieldCache.put(fieldName, field);
                } catch (NoSuchFieldException e) {
                    Log.w(LOG_TAG, e);
                }
            }
        }
        return field;
    }

    public static Object invokeDeclaredMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes,
                                              Object receiver, Object[] parameters) {
        Object result = null;
        if (clazz != null) {
            Method method = getDeclaredMethod(clazz, methodName, parameterTypes);
            if (method != null) {
                try {
                    result = method.invoke(receiver, parameters);
                } catch (InvocationTargetException e) {
                    Log.w(LOG_TAG, e);
                } catch (IllegalAccessException e) {
                    Log.w(LOG_TAG, e);
                }
            }
        }
        return result;
    }

    public static Object getDeclaredFieldValue(Class<?> clazz, String fieldName, Object receiver) {
        Object value = null;
        if (clazz != null) {
            final Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                try {
                    value = field.get(receiver);
                } catch (IllegalAccessException e) {
                    Log.w(LOG_TAG, e);
                }
            }
        }
        return value;
    }

    public static void setDeclaredFieldValue(Class<?> clazz, String fieldName, Object receiver, Object value) {
        if (clazz != null) {
            final Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                try {
                    field.set(receiver, value);
                } catch (IllegalAccessException e) {
                    Log.w(LOG_TAG, e);
                }
            }
        }
    }
}
