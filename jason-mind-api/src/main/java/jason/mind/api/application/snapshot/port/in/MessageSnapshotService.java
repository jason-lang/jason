package jason.mind.api.application.snapshot.port.in;

import jason.mind.api.application.snapshot.model.MessageSnapshot;

import java.util.Date;
import java.util.List;

public interface MessageSnapshotService {

    List<MessageSnapshot> findInMessagesUntilTime(String name, Date time);

    List<MessageSnapshot> findOutMessagesUntilTime(String name, Date time);

}
