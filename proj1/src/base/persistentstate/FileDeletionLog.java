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

    public boolean isEmpty() {
        return this.deletion_log.isEmpty();
    }

    public void addFile(String file_id) {
        deletion_log.put(file_id, ProtocolDefinitions.MOCK_HASHMAP_SET_VALUE);
    }

    public void joinLog(FileDeletionLog file_deletion_log) {
        System.out.println("Received a log with size: " + file_deletion_log.deletion_log.size());

        deletion_log.putAll(file_deletion_log.deletion_log);

        // Delete files that are supposed to be deleted
        for (String file_id : deletion_log.keySet()) {
            StorageManager.getInstance().removeFileChunksIfStored(file_id);
        }
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
            System.out.printf("Done reading file deletion log from disk, containing %d items.\n", instance.deletion_log.size());
        } catch (Exception ignored) {
        }
    }
}
