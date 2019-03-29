package base.tasks;

import base.messages.CommonMessage;
import base.Keyable;

public interface Task extends Keyable {
    void notify(CommonMessage msg);

    void communicate();
}
