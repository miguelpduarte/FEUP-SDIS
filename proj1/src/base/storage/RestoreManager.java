package base.storage;

import base.Keyable;

import java.util.concurrent.ConcurrentHashMap;

public class RestoreManager {
    private static RestoreManager instance = new RestoreManager();

    private final ConcurrentHashMap<String, Restorer> restorers = new ConcurrentHashMap<>();

    public static RestoreManager getInstance() {
        return instance;
    }

    private RestoreManager() {
    }

    public Restorer getRestorer(Keyable k) {
        return restorers.get(k.toKey());
    }

    public void registerRestorer(Restorer r) {
        this.restorers.put(r.toKey(), r);
    }

    public void unregisterRestorer(Keyable k) {
        this.restorers.remove(k.toKey());
    }
}
