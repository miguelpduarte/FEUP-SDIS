public class MDBMesssageHandler implements Runnable {
    private byte[] data;

    public MDBMesssageHandler(byte[] data) {
        this.data = data;
    }

    @Override
    public void run() {
        System.out.println("\t\tMDB: Starting message handling");
        CommonMessage info = MessageFactory.getBasicInfo(this.data);
        if (info == null) {
            System.out.println("MDB: Message couldn't be parsed");
            return;
        }

        System.out.printf("\t\tMDB: Received message of type %s\n", info.getMessageType().name());
        /*Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            t.notify(info);
        }*/
    }
}

