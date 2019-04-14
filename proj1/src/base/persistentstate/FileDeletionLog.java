package base.persistentstate;

import base.ProtocolDefinitions;
import base.storage.StorageManager;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileDeletionLog implements Serializable {
    private static FileDeletionLog instance = new FileDeletionLog();
    private ConcurrentHashMap<String, Boolean> deletion_log = new ConcurrentHashMap<>();

    public static FileDeletionLog getInstance() {
        return instance;
    }

    private FileDeletionLog() {
    }

    public boolean hasFile(String file_id) {
        return deletion_log.contains(file_id);
    }

    public void addFile(String file_id) {
        deletion_log.put(file_id, ProtocolDefinitions.MOCK_HASHMAP_SET_VALUE);
    }

    public void writeToDisk() {
        try (
                FileOutputStream fos = new FileOutputStream(StorageManager.getInstance().getFileDeletionLogPath());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(this);
        } catch (Exception ignored) {
        }
    }

    public void readFromDisk()  {
        if (new File(StorageManager.getInstance().getFileDeletionLogPath()).length() == 0) {
            System.out.println("File deletion log in disk was empty.");
            return;
        }

        try (
                FileInputStream fis = new FileInputStream(StorageManager.getInstance().getFileDeletionLogPath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            instance = (FileDeletionLog) ois.readObject();
            System.out.printf("Done reading file deletion log from disk, containing %d items:\n", instance.deletion_log.size());
            for (String s : instance.deletion_log.keySet()) {
                System.out.println(s);
            }
            System.out.println("\n");
        } catch (Exception ignored) {
        }
    }
}
