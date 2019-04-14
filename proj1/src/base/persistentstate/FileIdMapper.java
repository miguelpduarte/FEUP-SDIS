package base.persistentstate;

import base.storage.StorageManager;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileIdMapper {
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
        if (file_id_map.contains(file_name)) {
            file_id_map.remove(file_name);
        }
        file_id_map.put(file_name, file_id);
        writeMapInDisk();
    }

    private void writeMapInDisk() {
        try (
                FileOutputStream fos = new FileOutputStream(StorageManager.getInstance().getFileIdMapPath());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(this.file_id_map);
        } catch (Exception ignored) {
        }
    }

    public void readMapFromDisk()  {
        if (new File(StorageManager.getInstance().getFileIdMapPath()).length() == 0) {
            System.out.println("File id map in disk was empty.");
            return;
        }

        try (
                FileInputStream fis = new FileInputStream(StorageManager.getInstance().getFileIdMapPath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            ConcurrentHashMap<String, String> new_file_id_map = (ConcurrentHashMap<String, String>) ois.readObject();
            this.file_id_map = new_file_id_map;
            System.out.printf("Done reading file id map from disk, containing %d items.\n", this.file_id_map.size());
        } catch (Exception ignored) {
        }
    }
}
