package cn.myflv.android.noactive.server;

import de.robv.android.xposed.XposedHelpers;

public class Event {

    public final static String Event = "android.app.usage.UsageEvents.Event";
    public final static String ACTIVITY_RESUMED = "ACTIVITY_RESUMED";
    public final static String ACTIVITY_PAUSED = "ACTIVITY_PAUSED";
    public final static String ACTIVITY_STOPPED = "ACTIVITY_STOPPED";
    public final static String ACTIVITY_DESTROYED = "ACTIVITY_DESTROYED";

    public static Class<?> getEvent(ClassLoader classLoader) {
        return XposedHelpers.findClass(Event, classLoader);
    }

    public static int ACTIVITY_RESUMED(ClassLoader classLoader) {
        Class<?> Event = getEvent(classLoader);
        return XposedHelpers.getStaticIntField(Event, ACTIVITY_RESUMED);
    }

    public static int ACTIVITY_PAUSED(ClassLoader classLoader) {
        Class<?> Event = getEvent(classLoader);
        return XposedHelpers.getStaticIntField(Event, ACTIVITY_PAUSED);
    }
    public static int ACTIVITY_STOPPED(ClassLoader classLoader) {
        Class<?> Event = getEvent(classLoader);
        return XposedHelpers.getStaticIntField(Event, ACTIVITY_STOPPED);
    }

    public static int ACTIVITY_DESTROYED(ClassLoader classLoader) {
        Class<?> Event = getEvent(classLoader);
        return XposedHelpers.getStaticIntField(Event, ACTIVITY_DESTROYED);
    }
}
