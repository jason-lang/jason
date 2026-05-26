package jason.mind.api.application.snapshot.service;

import jason.mind.api.application.snapshot.model.MessageSnapshot;
import jason.mind.api.application.snapshot.port.in.MessageSnapshotService;
import jason.mind.api.application.snapshot.port.out.AgentMessageRepository;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class StandardMessageSnapshotService implements MessageSnapshotService {

    private final AgentMessageRepository messageRepository;

    @Override
    public List<MessageSnapshot> findInMessagesUntilTime(String name, Date time) {
        return this.messageRepository.findInMessagesUntil(name, time);
    }

    @Override
    public List<MessageSnapshot> findOutMessagesUntilTime(String name, Date time) {
        return this.messageRepository.findOutMessagesUntil(name, time);
    }
}
