package jason.mind.api.application.snapshot.port.out;

import jason.mind.api.application.snapshot.model.MessageSnapshot;

import java.util.Date;
import java.util.List;

public interface AgentMessageRepository {

    List<MessageSnapshot> findInMessagesUntil(String agentName, Date time);

    List<MessageSnapshot> findOutMessagesUntil(String agentName, Date time);

    List<MessageSnapshot> findAllMessages();
}
