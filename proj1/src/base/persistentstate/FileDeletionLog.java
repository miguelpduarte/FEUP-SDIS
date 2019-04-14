package base.persistentstate;

import base.storage.StorageManager;

import java.io.*;
import java.util.HashSet;

public class FileDeletionLog {
    private static FileDeletionLog instance = new FileDeletionLog();
    private HashSet<String> deletion_log = new HashSet<>();

    public static FileDeletionLog getInstance() {
        return instance;
    }

    private FileDeletionLog() {
    }

    public boolean hasFile(String file_id) {
        return deletion_log.contains(file_id);
    }

    public void addFile(String file_id) {
        deletion_log.add(file_id);
    }

    public void writeLogToDisk() {
        try (
                FileOutputStream fos = new FileOutputStream(StorageManager.getInstance().getFileDeletionLogPath());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(this.deletion_log);
        } catch (Exception ignored) {
        }
    }

    public void readLogFromDisk()  {
        if (new File(StorageManager.getInstance().getFileDeletionLogPath()).length() == 0) {
            System.out.println("File deletion log in disk was empty.");
            return;
        }

        try (
                FileInputStream fis = new FileInputStream(StorageManager.getInstance().getFileDeletionLogPath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            this.deletion_log = (HashSet<String>) ois.readObject();
            System.out.printf("Done reading file deletion log from disk, containing %d items.\n", this.deletion_log.size());
        } catch (Exception ignored) {
        }
    }
}
