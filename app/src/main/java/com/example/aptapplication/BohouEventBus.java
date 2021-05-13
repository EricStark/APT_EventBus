package com.example.aptapplication;

/**
 * Created by bohou on 2021/4/6 Email:bohou@tencent.com
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.apt_annotations.SubscribMethod;
import com.example.apt_annotations.Subscribe;
import com.example.apt_annotations.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

public class BohouEventBus {

    private static volatile BohouEventBus instance;

    private static final String BOHOUEVENTBUS = "BOHOUEVENTBUS";
    private static final int COREPOOLSIZE = 3;
    private static final int MAXIMUMPOOLSIZE = 5;
    private static final long KEEPALIVETIME = 2000;

    private static Map<Object, List<SubscribMethod>> cacheMap;
    private Handler mHandler;
    private final ThreadPoolExecutor executor;

    private BohouEventBus() {
        cacheMap = new HashMap<>();
        mHandler = new Handler(Looper.getMainLooper());
        executor = new ThreadPoolExecutor(COREPOOLSIZE
                , MAXIMUMPOOLSIZE, KEEPALIVETIME, TimeUnit.MILLISECONDS
                , new SynchronousQueue<Runnable>()
                , Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public static BohouEventBus getDefaultEventbus() {
        if (instance == null) {
            synchronized (BohouEventBus.class) {
                if (instance == null) {
                    instance = new BohouEventBus();
                }
            }
        }
        return instance;
    }

    /**
     * @param who registered class
     */
    public synchronized void register(Object who) {
        if (who == null) {
            try {
                Log.e(BOHOUEVENTBUS, "The object passed in cannot be null");
                throw new RuntimeException("The object passed in cannot be null");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        List<SubscribMethod> list = cacheMap.get(who);
        //explain that this class is not registered
        if (list == null) {
            /**
             * return the list of methods registered by this class through reflection
             */
            //list = getSubcribeMethodFlection(who);
            /**
             * generate code through APT
             */
            list = APTBohouEventBusPolicy.getSubscribeMethod(who.getClass());
            if (list.size() > 0) {
                cacheMap.put(who, list);
                Log.d(BOHOUEVENTBUS, "registration success");
            } else {
                Log.e(BOHOUEVENTBUS, "There are no methods to be registered in this class");
            }
        }
    }

    /**
     * Get the method of registration of this class through reflection
     *
     * @param obj
     * @return
     */
    private List<SubscribMethod> getSubcribeMethodFlection(Object obj) {
        List<SubscribMethod> list = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            String clazzName = clazz.getName();
            if (clazzName.startsWith("java") || clazzName.startsWith("javax")
                    || clazzName.startsWith("android") || clazzName.startsWith("androidx")) {
                break;
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                if (annotation == null) {
                    continue;
                } else {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        throw new RuntimeException("there's problem in method params");
                    }
                    ThreadMode threadMode = annotation.threadMode();
                    SubscribMethod subscribMethod = new SubscribMethod(method, parameterTypes[0], threadMode);
                    list.add(subscribMethod);
                }
            }
            /**
             * See if the parent class has the annotation modification
             */
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    /**
     * @param event
     */
    public synchronized void post(final Object event) {
        Set<Object> registedClassesSet = cacheMap.keySet();
        Iterator<Object> iterator = registedClassesSet.iterator();
        while (iterator.hasNext()) {
            /**
             * Get the registered class
             */
            final Object registedClass = iterator.next();
            /**
             * Get the list of methods registered for this class
             */
            List<SubscribMethod> subscribMethods = cacheMap.get(registedClass);
            for (final SubscribMethod method : subscribMethods) {
                if (method.getEventType().isAssignableFrom(event.getClass())) {
                    switch (method.getThreadMode()) {
                        /**
                         * The event is processed in the main thread
                         * then judge the post thread
                         */
                        case MAIN_THREAD:
                            //The event is post in the main thread
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(method, registedClass, event);
                            } else {
                                postToMainThread(method, registedClass, event);
                            }
                            break;
                        /**
                         * The event is processed in the child thread
                         * then judge the post thread
                         */
                        case SYNC_THREAD:
                            //The event is post in the main thread
                            if (Looper.getMainLooper() == Looper.myLooper()) {
                                postToChildThread(method, registedClass, event);
                            } else {
                                invoke(method, registedClass, event);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void postToMainThread(final SubscribMethod method, final Object registedClass, final Object event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invoke(method, registedClass, event);
            }
        });
    }

    private void postToChildThread(final SubscribMethod method, final Object registedClass, final Object event) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                invoke(method, registedClass, event);
            }
        });
    }

    private void invoke(SubscribMethod subscribeMethod, Object registedClass, Object event) {
        Method method = subscribeMethod.getMethod();
        if (method == null) {
            return;
        }
        method.setAccessible(true);
        try {
            Log.d(BOHOUEVENTBUS, "invoke method" + method.getName());
            method.invoke(registedClass, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
