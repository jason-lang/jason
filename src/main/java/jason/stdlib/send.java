package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.concurrent.TimeUnit;

/**
  <p>Internal action: <b><code>.send</code></b>.

  <p>Description: sends a message to an agent.

  <p>Parameters:<ul>

  <li>+ receiver (atom, string, or list): the receiver of the
  message. It is the unique name of the agent that will receive the
  message (or list of names).<br/>

  <li>+ ilf (atom): the illocutionary force of the message (tell,
  achieve, ...).<br/>

  <li>+ message (literal): the content of the message.<br/>

  <li><i>+ answer</i> (any term [optional]): the answer of an ask
  message (for performatives askOne, askAll, and askHow).<br/>

  <li><i>+ timeout</i> (number [optional]): timeout (in milliseconds)
  when waiting for an ask answer.<br/>

  </ul>

  <p>Messages with an <b>ask</b> illocutionary force can optionally have
  arguments 3 and 4. In case they are given, <code>.send</code> suspends the
  intention until an answer is received and unified with <code>arg[3]</code>,
  or the message request times out as specified by
  <code>arg[4]</code>. Otherwise, the intention is not suspended and the
  answer (which is a tell message) produces a belief addition event as usual.

  <p>Examples (suppose that agent <code>jomi</code> is sending the
  messages):<ul>

  <li> <code>.send(rafael,tell,value(10))</code>: sends <code>value(10)</code>
  to the agent named <code>rafael</code>. The literal
  <code>value(10)[source(jomi)]</code> will be added as a belief in
  <code>rafael</code>'s belief base.</li>

  <li> <code>.send(rafael,achieve,go(10,30)</code>: sends
  <code>go(10,30)</code> to the agent named <code>rafael</code>. When
  <code>rafael</code> receives this message, an event
  <code>&lt;+!go(10,30)[source(jomi)],T&gt;</code> will be added in
  <code>rafael</code>'s event queue.</li>

  <li> <code>.send(rafael,askOne,value(beer,X))</code>: sends
  <code>value(beer,X)</code> to the agent named rafael. This askOne is an
  asynchronous ask since it
  does not suspend jomi's intention. If rafael has, for instance, the literal
  <code>value(beer,2)</code>
  in its belief base, this belief is automatically sent back to jomi. Otherwise an event
  like <code>+?value(beer,X)[source(self)]</code> is generated in rafael's side
  and the result of this query is then sent to jomi. In the jomi's side, the rafael's answer
  is added in the jomi's belief base and an event like
  <code>+value(beer,10)[source(rafael)]</code> is generated.</li>

  <li> <code>.send(rafael,askOne,value(beer,X),A)</code>: sends
  <code>value(beer,X)</code> to the agent named <code>rafael</code>. This askOne
  is a synchronous askOne, it suspends <code>jomi</code>'s intention until
  <code>rafael</code>'s
  answer is received. The answer (something like <code>value(beer,10)</code>)
  unifies with <code>A</code>.</li>

  <li> <code>.send(rafael,askOne,value(beer,X),A,2000)</code>: as in the
  previous example, but agent <code>jomi</code> waits for 2 seconds. If no
  message is received by then, <code>A</code> unifies with
  <code>timeout</code>.</li>

  </ul>

  @see jason.stdlib.broadcast
  @see jason.stdlib.my_name

*/
public class send extends DefaultInternalAction {

    @Override
    public boolean canBeUsedInContext() {
        return false;
    }

    private boolean lastSendWasSynAsk = false;

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 5;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isAtom() && !args[0].isList() && !args[0].isString())
            throw JasonException.createWrongArgument(this,"TO parameter ('"+args[0]+"') must be an atom, a string or a list of receivers!");

        if (! args[1].isAtom())
            throw JasonException.createWrongArgument(this,"illocutionary force parameter ('"+args[1]+"') must be an atom!");
    }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        final Term to   = args[0];
        Term ilf  = args[1];
        Term pcnt = args[2];

        // remove source annots in the content (in case it is a pred)
        // -- CHANGE: use nested annots
        //try {
        //    ((Pred)pcnt).delSources();
        //} catch (Exception e) {}


        // create a message to be sent
        final Message m = new Message(ilf.toString(), ts.getUserAgArch().getAgName(), null, pcnt);

        // async ask has a fourth argument and should suspend the intention
        lastSendWasSynAsk = m.isAsk() && args.length > 3;
        if (lastSendWasSynAsk) {
            m.setSyncAskMsgId();
            ts.getC().addPendingIntention(m.getMsgId(), ts.getC().getSelectedIntention());
        }

        // (un)tell or unknown performative with 4 args is a reply to
        if ( (m.isTell() || m.isUnTell() || !m.isKnownPerformative()) && args.length > 3) {
            Term mid = args[3];
            if (! mid.isAtom()) {
                throw new JasonException("The Message ID ('"+mid+"') parameter of the internal action 'send' is not an atom!");
            }
            m.setInReplyTo(mid.toString());
        }

        // send the message
        if (to.isList()) {
            for (Term t: (ListTerm)to) {
                delegateSendToArch(t, ts, m);
            }
        } else {
            delegateSendToArch(to, ts, m);
        }

        if (lastSendWasSynAsk && args.length == 5) {
            // get the timeout deadline
            Term tto = args[4];
            if (tto.isNumeric()) {
                Agent.getScheduler().schedule( new Runnable() {
                    public void run() {
                        // if the intention is still in PI, brings it back to C.I with the timeout
                        Intention intention = ts.getC().removePendingIntention(m.getMsgId());
                        if (intention != null) {
                            // unify "timeout" with the fourth parameter of .send
                            Structure send = (Structure)intention.peek().removeCurrentStep();
                            Term timeoutAns = null;
                            if (to.isList()) {
                                VarTerm answers = new VarTerm("AnsList___"+m.getMsgId());
                                Unifier un = intention.peek().getUnif();
                                timeoutAns = un.get(answers);
                                if (timeoutAns == null)
                                    timeoutAns = new ListTermImpl();
                            } else {
                                timeoutAns = new Atom("timeout");
                            }
                            intention.peek().getUnif().unifies(send.getTerm(3), timeoutAns);
                            // add the intention back in C.I
                            ts.getC().resumeIntention(intention);
                            ts.getUserAgArch().wakeUpAct();
                        }
                    }
                }, (long)((NumberTerm)tto).solve(), TimeUnit.MILLISECONDS);
            } else {
                throw new JasonException("The 5th parameter of send must be a number (timeout) and not '"+tto+"'!");
            }
        }

        return true;
    }

    private void delegateSendToArch(Term to, TransitionSystem ts, Message m) throws Exception {
        if (!to.isAtom() && !to.isString())
            throw new JasonException("The TO parameter ('"+to+"') of the internal action 'send' is not an atom!");

        String rec = null;
        if (to.isString())
            rec = ((StringTerm)to).getString();
        else
            rec = to.toString();
        if (rec.equals("self"))
            rec = ts.getUserAgArch().getAgName();
        m.setReceiver(rec);
        ts.getUserAgArch().sendMsg(m);
    }

    @Override
    public boolean suspendIntention() {
        return lastSendWasSynAsk;
    }
}
