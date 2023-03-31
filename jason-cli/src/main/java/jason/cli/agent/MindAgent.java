package jason.cli.agent;

import jason.asSemantics.Intention;
import jason.cli.mas.RunningMASs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.rmi.RemoteException;


@Command(
    name = "mind",
    description = "inspects the mind of an agent"
)
public class MindAgent implements Runnable {

    @CommandLine.Parameters(paramLabel = "<agent name>", defaultValue = "",
            arity = "1",
            description = "agent unique identification")
    String agName;

    @CommandLine.ParentCommand
    protected Agent parent;

    @CommandLine.Option(names = { "--no-beliefs" }, defaultValue = "false", description = "do not show beliefs")
    boolean noBeliefs;

    @CommandLine.Option(names = { "--plans" }, defaultValue = "false", description = "show plans")
    boolean plans;

    @CommandLine.Option(names = { "--intentions" }, defaultValue = "false", description = "show intentions")
    boolean intentions;

    @CommandLine.Option(names = { "--mas-name" }, paramLabel = "<mas name>", defaultValue = "", description = "MAS unique identification")
    String masName;

    @Override
    public void run() {
        if (!RunningMASs.isRunningMAS(masName)) {
            parent.parent.errorMsg("no running MAS, so, no agent to inspect.");
            return;
        }
        if (agName.isEmpty()) {
            parent.parent.errorMsg("the name of the agent should be informed, e.g., 'agent mind bob'.");
            return;
        }
        if (!RunningMASs.hasAgent(masName, agName)) {
            parent.parent.errorMsg("the agent with name " + agName + " is not running!");
            return;
        }

        try {
            var ag = RunningMASs.getRTS(masName).getAgentSnapshot(agName);
            if (!noBeliefs)
                showBeliefs(ag);

            if (plans)
                showPlans(ag);

            if (intentions)
                showIntentions(ag);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    void showBeliefs(jason.asSemantics.Agent ag) {
        var out = new StringBuilder();
        for (var ns: ag.getBB().getNameSpaces()) {
            if (ns.toString().equals("kqml"))
                continue;
            var first = true;
            for (var b: ag.getBB()) {
                if (b.getNS().equals(ns)) {
                    if (first) {
                        if (!ns.toString().equals("default"))
                            out.append(ns+"::\n");
                        first = false;
                    }
                    // remove namespace
                    var bs = b.toString();
                    var p  = bs.indexOf("::");
                    if (p>0)
                        bs = bs.substring(p+2);
                    out.append("    "+bs+"\n");
                }
            }
        }
        parent.parent.println( out.toString());
    }

    void showPlans(jason.asSemantics.Agent ag) {
        parent.parent.println( ag.getPL().getAsTxt(false).trim());
    }

    void showIntentions(jason.asSemantics.Agent ag) {
        var ii = ag.getTS().getC().getAllIntentions();
        while (ii.hasNext()) {
            Intention i = ii.next();
            parent.parent.println(i+"state: "+i.getStateBasedOnPlace());
        }
    }

}

