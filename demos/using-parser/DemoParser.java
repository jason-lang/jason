import java.io.*;
import jason.asSyntax.parser.*;
import jason.mas2j.parser.*;
import jason.mas2j.*;
import jason.asSemantics.*;

public class DemoParser {
    public static void main(String[] args) throws Exception {
        asl();
        mas2j();
        System.exit(0);
    }

    public static void asl() throws Exception {
        System.out.println("*** ASL ***");
        // create a parser from a file (it could be any kind of stream)
        var parser = new as2j(new FileInputStream("r1.asl"));

        // create an Agent where to place the result of the parser
        var ag = new Agent();
        ag.initAg();

        // run the parser
        parser.agent(ag);

        // print what was parsed
        System.out.println("Initial beliefs: "+ag.getInitialBels());
        System.out.println("Initial goals: "+ag.getInitialGoals());

        for (var plan: ag.getPL()) {
            System.out.println("\nPlan: "+plan);
        }
    }

    public static void mas2j() throws Exception {
        System.out.println("\n\n*** MAS2J ***");

        // create a parser from a file (it could be any kind of stream)
        var parser = new mas2j(new FileInputStream("mars.mas2j"));


        // run the parser
        MAS2JProject project = parser.mas();
        for (var ag: project.getAgents()) {
            System.out.println("Agent: "+ag+" from "+ag.getSource());
        }

        System.out.println("\nEnv: "+project.getEnvClass());
    }

}
