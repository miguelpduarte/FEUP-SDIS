package base.channels;

public class ChannelManager {
    private static ChannelManager instance = new ChannelManager();
    private ControlChannelHandler control;
    private BackupChannelHandler backup;
    private RestoreChannelHandler restore;

    public static ChannelManager getInstance() {
        return instance;
    }

    private ChannelManager() {
    }

    public void setChannelHandlers(ControlChannelHandler control, BackupChannelHandler backup, RestoreChannelHandler restore) {
        this.control = control;
        this.backup = backup;
        this.restore = restore;
    }

    public ControlChannelHandler getControl() {
        return control;
    }

    public BackupChannelHandler getBackup() {
        return backup;
    }

    public RestoreChannelHandler getRestore() {
        return restore;
    }
}
