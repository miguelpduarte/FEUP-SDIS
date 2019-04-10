package base.shutdownandstartup;

import base.storage.ChunkBackupState;

import java.io.Serializable;

public class RestoreInformationWrapper implements Serializable {

    private final ChunkBackupState cbs_instance;
    private final int occupied_space_bytes;
    private final Object placeholder;

    public RestoreInformationWrapper(ChunkBackupState cbs_instance, int occupied_space_bytes, Object placeholder) {
        this.cbs_instance = cbs_instance;
        this.occupied_space_bytes = occupied_space_bytes;
        this.placeholder = placeholder;
    }

    public ChunkBackupState getCbsInstance() {
        return cbs_instance;
    }

    public int getOccupiedSpaceBytes() {
        return occupied_space_bytes;
    }
}
