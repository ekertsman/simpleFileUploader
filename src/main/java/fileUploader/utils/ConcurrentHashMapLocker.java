package fileUploader.utils;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapLocker {
    static private ConcurrentHashMap<String, Object> lockerMap;

    static {
        lockerMap = new ConcurrentHashMap<String, Object>();
    }

    public static Object getLockerString(String key){
        Object locker = lockerMap.getOrDefault(key, null);
        if (locker == null){
            Object newLock = new Object();
            Object existingLock = lockerMap.putIfAbsent(key, newLock);
            locker = existingLock == null ? newLock : existingLock;
        }
        return locker;
    }
}
