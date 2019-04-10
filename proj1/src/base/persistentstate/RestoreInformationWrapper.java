package base.persistentstate;

import base.storage.stored.ChunkBackupState;
import base.storage.requested.RequestedBackupsState;

import java.io.Serializable;

public class RestoreInformationWrapper implements Serializable {

    private final ChunkBackupState cbs_instance;
    private final int occupied_space_bytes;
    private final RequestedBackupsState rbs_instance;

    public RestoreInformationWrapper(ChunkBackupState cbs_instance, int occupied_space_bytes, RequestedBackupsState rbs_instance) {
        this.cbs_instance = cbs_instance;
        this.occupied_space_bytes = occupied_space_bytes;
        this.rbs_instance = rbs_instance;
    }

    public ChunkBackupState getCbsInstance() {
        return cbs_instance;
    }

    public int getOccupiedSpaceBytes() {
        return occupied_space_bytes;
    }

    public RequestedBackupsState getRbsInstance() {
        return rbs_instance;
    }
}
