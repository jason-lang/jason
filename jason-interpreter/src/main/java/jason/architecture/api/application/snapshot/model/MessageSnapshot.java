package jason.architecture.api.application.snapshot.model;

import jason.architecture.api.application.snapshot.model.term.TermWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
@Getter
public class MessageSnapshot {

    private final String id;

    private final String illocutionaryForce;

    private final TermWrapper content;

    private final String sender;

    private final String receiver;

    private final String inReplyTo;

    private final int receivedCycle;

    private final Date time;

}
