package base.storage.requested;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class RequestedBackupsState implements Serializable {
    private static RequestedBackupsState instance = new RequestedBackupsState();

    public static RequestedBackupsState getInstance() {
        return instance;
    }

    private RequestedBackupsState() {
    }

    public static void setInstance(RequestedBackupsState instance) {
        RequestedBackupsState.instance = instance;
    }

    private final ConcurrentHashMap<String, RequestedBackupFile> backed_up_files = new ConcurrentHashMap<>();
    private static final RequestedBackupFile null_file_backup = new NullRequestedBackupFile();

    public void registerRequestedFile(RequestedBackupFile rbf) {
        this.backed_up_files.put(rbf.getFileId(), rbf);
    }

    public RequestedBackupFile getRequestedFileBackupInfo(String file_id) {
        return this.backed_up_files.getOrDefault(file_id, null_file_backup);
    }

    public void unregisterRequestedFile(RequestedBackupFile rbf) {
        this.backed_up_files.remove(rbf.getFileId());
    }

    public void unregisterRequestedFile(String file_id) {
        this.backed_up_files.remove(file_id);
    }

    public boolean didRequestBackup(String file_id) {
        return this.backed_up_files.containsKey(file_id);
    }

    public Collection<RequestedBackupFile> getAllFilesInfo() {
        return this.backed_up_files.values();
    }
}
