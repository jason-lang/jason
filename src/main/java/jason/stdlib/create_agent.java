package jason.stdlib;

import java.util.ArrayList;
import java.util.List;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

/**
  <p>Internal action: <b><code>.create_agent</code></b>.

  <p>Description: creates another agent using the referred AgentSpeak source
  code.

  <p>Parameters:<ul>

  <li>+ name (atom, string, or variable): the name for the new agent.
  If this parameter is a variable, it will be unified with the name given to the agent.
  The agent's name will be the name of the variable and some number that makes it unique.<br/>

  <li><i>+ source</i> (string): path to the file where the AgentSpeak code
  for the new agent can be found.<br/>

  <li><i>+ customisations</i> (list -- optional): list of optional parameters
  as agent class, architecture and belief base.<br/>

  </ul>

  <p>Examples:<ul>

  <li> <code>.create_agent(bob,"/tmp/x.asl")</code>: creates an agent named "bob"
  from the source file in "/tmp/x.asl".</li>

  <li> <code>.create_agent(Bob,"/tmp/x.asl")</code>: creates an agent named "bob" (or "bob_1", "bob_2", ...)
  and unifies variable Bob with the given name.</li>

  <li>
  <code>.create_agent(bob,"x.asl", [agentClass("myp.MyAgent")])</code>:
  creates the agent with customised agent class
  <code>myp.MyAgent</code>.</li>

  <code>.create_agent(bob,"x.asl", [agentArchClass("myp.MyArch")])</code>:
  creates the agent with customised architecture class
  <code>myp.MyArch</code>.</li>

  <code>.create_agent(bob,"x.asl", [beliefBaseClass("jason.bb.TextPersistentBB")])</code>:
  creates the agent with customised belief base
  <code>jason.bb.TextPersistentBB</code>.</li>

  <code>.create_agent(bob,"x.asl", [agentClass("myp.MyAgent"),
  agentArchClass("myp.MyArch"),
  beliefBaseClass("jason.bb.TextPersistentBB")])</code>: creates the
  agent with agent, architecture and belief base customised.</li>

  </ul>

  @see jason.stdlib.kill_agent
  @see jason.stdlib.save_agent
  @see jason.stdlib.stopMAS
  @see jason.runtime.RuntimeServicesInfraTier
*/
public class create_agent extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length > 1 && !args[1].isString())
            throw JasonException.createWrongArgument(this,"second argument must be a string");
        if (args.length == 3 && !args[2].isList())
            throw JasonException.createWrongArgument(this,"third argument must be a list");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        String name = getName(args);
        String source = getSource(args);

        List<String> agArchClasses = getAgArchClasses(args);

        String agClass = null;
        ClassParameters bbPars = null;
        if (args.length > 2) { // optional parameter
            // get the parameters
            for (Term t: (ListTerm)args[2]) {
                if (t.isStructure()) {
                    Structure s = (Structure)t;
                    if (s.getFunctor().equals("beliefBaseClass")) {
                        bbPars = new ClassParameters(testString(s.getTerm(0)));
                    } else if (s.getFunctor().equals("agentClass")) {
                        agClass = testString(s.getTerm(0)).toString();
                    }
                }
            }
        }
        RuntimeServicesInfraTier rs = ts.getUserAgArch().getRuntimeServices();
        name = rs.createAgent(name, source, agClass, agArchClasses, bbPars, getSettings(ts), ts.getAg());
        rs.startAgent(name);

        if (args[0].isVar())
            return un.unifies(new StringTermImpl(name), args[0]);
        else
            return true;
    }

    protected Settings getSettings(TransitionSystem ts) {
        /*Settings s = ts.getSettings();
        s.removeUserParameter(Settings.INIT_BELS);
        s.removeUserParameter(Settings.INIT_GOALS);
        return s;*/
        return new Settings();
    }

    protected String getName(Term[] args) {
        String name;
        if (args[0].isString())
            name = ((StringTerm)args[0]).getString();
        else
            name = args[0].toString();

        if (args[0].isVar())
            name = name.substring(0,1).toLowerCase() + name.substring(1);
        return name;
    }

    protected String getSource(Term[] args) throws JasonException {
        String source = null;
        if (args.length > 1) {
            source = ((StringTerm)args[1]).getString();
        }
        return source;
    }

    protected List<String> getAgArchClasses(Term[] args) {
        List<String> agArchClasses = new ArrayList<String>();
        if (args.length > 2) { // optional parameter
            // get the parameters
            for (Term t: (ListTerm)args[2]) {
                if (t.isStructure()) {
                    Structure s = (Structure)t;
                    if (s.getFunctor().equals("agentArchClass")) {
                        agArchClasses.add(testString(s.getTerm(0)).toString());
                    }
                }
            }
        }
        return agArchClasses;
    }

    private Structure testString(Term t) {
        if (t.isStructure())
            return (Structure)t;
        if (t.isString())
            return Structure.parse(((StringTerm)t).getString());
        return null;
    }
}
