package jason.mind.api.infrastructure.mapper.message;

import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.Term;
import jason.infra.local.LocalAgArch;
import jason.mind.api.application.snapshot.model.MessageSnapshot;
import jason.mind.api.application.snapshot.model.term.TermWrapper;
import jason.mind.api.infrastructure.adapter.out.jason.JasonUtils;
import jason.mind.api.infrastructure.mapper.term.TermMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class StandardMessageSnapshotMapper implements MessageSnapshotMapper {

    private final TermMapper termMapper;

    public StandardMessageSnapshotMapper(TermMapper termMapper) {
        this.termMapper = termMapper;
    }

    @Override
    public List<MessageSnapshot> extractAll(Agent agent, Date time) {
        LocalAgArch localAgArch = JasonUtils.getLocalAgArch(agent.getTS().getAg());
        Collection<Message> messages = localAgArch.getMBox();
        List<MessageSnapshot> messageSnapshots = new ArrayList<>();

        for (Message message : messages) {
            var snapshot = this.extract(agent, message, time);
            if (snapshot != null) {
                messageSnapshots.add(snapshot);
            }
        }

        return messageSnapshots;
    }

    @Override
    public MessageSnapshot extract(Agent agent, Message message, Date time) {
        List<TermWrapper> wrapper = new ArrayList<>();
        termMapper.extractAll(wrapper, (Term) message.getPropCont(), null);
        if (wrapper.isEmpty()) {
            return null;
        }

        return new MessageSnapshot(message.getMsgId(), message.getIlForce(), wrapper.get(0), message.getSender(),
                message.getReceiver(), message.getInReplyTo(), agent.getTS().getAgArch().getCycleNumber(), time);
    }
}
