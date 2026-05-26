package jason.mind.api.infrastructure.mapper.message;

import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.mind.api.application.snapshot.model.MessageSnapshot;

import java.util.Date;
import java.util.List;

public interface MessageSnapshotMapper {
    List<MessageSnapshot> extractAll(Agent agent, Date time);

    MessageSnapshot extract(Agent agent, Message message, Date time);
}
