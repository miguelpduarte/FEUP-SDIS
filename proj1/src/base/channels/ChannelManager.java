package base.channels;

public class ChannelManager {
    private static ChannelManager instance = new ChannelManager();
    private ChannelHandler control;
    private ChannelHandler backup;
    private ChannelHandler restore;

    public static ChannelManager getInstance() {
        return instance;
    }

    private ChannelManager() {
    }

    public void setChannelHandlers(ChannelHandler control, ChannelHandler backup, ChannelHandler restore) {
        this.control = control;
        this.backup = backup;
        this.restore = restore;
    }

    public ChannelHandler getControl() {
        return control;
    }

    public ChannelHandler getBackup() {
        return backup;
    }

    public ChannelHandler getRestore() {
        return restore;
    }
}
