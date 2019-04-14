package base.persistentstate;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.storage.stored.ChunkBackupState;
import base.storage.requested.RequestedBackupsState;
import base.storage.StorageManager;

import java.io.*;

public class ShutdownAndStartupHandler {
    public static void restoreOnStartup() throws IOException, ClassNotFoundException {
        if (new File(StorageManager.getInstance().getChunkBackupInformationPath()).length() == 0) {
            return;
        }

        try (
                FileInputStream fis = new FileInputStream(StorageManager.getInstance().getChunkBackupInformationPath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            final RestoreInformationWrapper riw = (RestoreInformationWrapper) ois.readObject();
            ChunkBackupState.setInstance(riw.getCbsInstance());
            RequestedBackupsState.setInstance(riw.getRbsInstance());
            StorageManager.getInstance().setOccupiedSpaceBytes(riw.getOccupiedSpaceBytes());
        }
    }

    public static void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                backup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void startPeriodicBackupService() {
        periodicBackupService();
    }

    private static void periodicBackupService() {
        ThreadManager.getInstance().executeLater(() -> {
            try {
                System.out.println("Executing periodic backup");
                backup();
                periodicBackupService();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, ProtocolDefinitions.BACKUP_INTERVAL_SECONDS);
    }

    private static void backup() throws IOException {
        FileIdMapper.getInstance().writeToDisk();
        FileDeletionLog.getInstance().writeToDisk();

        try (
                FileOutputStream fos = new FileOutputStream(StorageManager.getInstance().getChunkBackupInformationPath());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(new RestoreInformationWrapper(
                    ChunkBackupState.getInstance(), StorageManager.getInstance().getOccupiedSpaceBytes(), RequestedBackupsState.getInstance()
            ));
        }
    }
}
