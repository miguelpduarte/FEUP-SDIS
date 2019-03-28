public interface Task extends Keyable {
    void notify(CommonMessage msg);

    void communicate();
}
