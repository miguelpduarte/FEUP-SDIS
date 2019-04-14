package base.persistentstate;

import base.protocol.task.DeleteTask;
import base.protocol.task.TaskManager;
import base.storage.StorageManager;
import base.storage.requested.RequestedBackupsState;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileIdMapper implements Serializable {
    private static FileIdMapper instance = new FileIdMapper();
    private ConcurrentHashMap<String, String> file_id_map = new ConcurrentHashMap<>();

    public static FileIdMapper getInstance() {
        return instance;
    }

    private FileIdMapper() {
    }

    public String getFileId(String file_name) {
        return file_id_map.get(file_name);
    }

    public void putFile(String file_name, String file_id) {
        String old_file_id = file_id_map.put(file_name, file_id);

        if (old_file_id != null) {
            System.out.println("New version for file received, deleting previous file_id from network and local storage.");
            final DeleteTask t = new DeleteTask(old_file_id);
            TaskManager.getInstance().registerTask(t);
            t.start();
            // Also deleting own files if they exist
            StorageManager.getInstance().removeFileChunksIfStored(old_file_id);
            RequestedBackupsState.getInstance().unregisterRequestedFile(old_file_id);
        }
    }

    public void removeFile(String file_name) {
        file_id_map.remove(file_name);
    }

    public void writeToDisk() {
        try (
                FileOutputStream fos = new FileOutputStream(StorageManager.getInstance().getFileIdMapPath());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(this);
        } catch (Exception ignored) {
        }
    }

    public void readFromDisk() {
        if (new File(StorageManager.getInstance().getFileIdMapPath()).length() == 0) {
            System.out.println("File id map in disk was empty.");
            return;
        }

        try (
                FileInputStream fis = new FileInputStream(StorageManager.getInstance().getFileIdMapPath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            instance = (FileIdMapper) ois.readObject();
            System.out.printf("Done reading file id map from disk, containing %d items.\n", instance.file_id_map.size());
        } catch (Exception ignored) {
        }
    }
}
