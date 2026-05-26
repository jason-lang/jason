package jason.architecture.api.infrastructure.mapper;

import jason.architecture.api.infrastructure.adapter.out.jason.JasonUtils;
import jason.architecture.api.application.snapshot.model.MessageSnapshot;
import jason.architecture.api.application.snapshot.model.term.TermWrapper;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.Term;
import jason.infra.local.LocalAgArch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MessageSnapshotMapper {

    private final Agent agent;

    private final Date time;

    public MessageSnapshotMapper(Agent agent, Date time) {
        this.agent = agent;
        this.time = time;
    }

    public List<MessageSnapshot> extractAll() {
        LocalAgArch localAgArch = JasonUtils.getLocalAgArch(this.agent.getTS().getAg());
        Collection<Message> messages = localAgArch.getMBox();
        List<MessageSnapshot> messageSnapshots = new ArrayList<>();

        for (Message message : messages) {
            messageSnapshots.add(this.extract(message));
        }

        return messageSnapshots;
    }

    public MessageSnapshot extract(Message message) {
        TermMapper termMapper = new TermMapper();

        List<TermWrapper> wrapper = new ArrayList<>();
        termMapper.extractAll(wrapper, (Term) message.getPropCont(), null);

        return new MessageSnapshot(message.getMsgId(), message.getIlForce(), wrapper.get(0), message.getSender(),
                message.getReceiver(), message.getInReplyTo(), this.agent.getTS().getAgArch().getCycleNumber(),
                this.time);
    }

}
