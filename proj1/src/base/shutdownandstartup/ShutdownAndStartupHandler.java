package base.shutdownandstartup;

import base.storage.ChunkBackupState;
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
            StorageManager.getInstance().setOccupiedSpaceBytes(riw.getOccupiedSpaceBytes());
        }
    }

    private static void backupOnShutdown() throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(StorageManager.getInstance().getChunkBackupInformationPath());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(new RestoreInformationWrapper(ChunkBackupState.getInstance(), StorageManager.getInstance().getOccupiedSpaceBytes(), null));
        }
    }

    public static void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ShutdownAndStartupHandler.backupOnShutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
