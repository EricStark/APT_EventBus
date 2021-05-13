package com.example.aptapplication;

import com.example.apt_annotations.SubscribMethod;
import com.example.apt_annotations.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.apt_annotations.ThreadMode.MAIN_THREAD;

/**
 * 规则--模板
 */
public class BohouEventBusIndex {

    private static Map<Object, List<SubscribMethod>> cacheMap = new HashMap<>();

    public static List<SubscribMethod> getSubscribeMethod(Object obj) {
        return cacheMap.get(obj);
    }

    static {
        setCacheMap();
    }

    public static void setCacheMap() {
        cacheMap.put(MainActivity.class, getMainSubscribeMethod());
        cacheMap.put(SecondActivity.class,getSecondSubscribeMethod());
    }

    public static List<SubscribMethod> getMainSubscribeMethod() {
        List<SubscribMethod> list = new ArrayList<>();

        list.add(new SubscribMethod(MainActivity.class, "onRecieve", Student.class, MAIN_THREAD));
        list.add(new SubscribMethod(MainActivity.class, "recieve", Teacher.class, MAIN_THREAD));
        return list;
    }

    public static List<SubscribMethod> getSecondSubscribeMethod() {
        List<SubscribMethod> list = new ArrayList<>();

        list.add(new SubscribMethod(MainActivity.class, "recieve", Teacher.class, MAIN_THREAD));
        return list;
    }
}
