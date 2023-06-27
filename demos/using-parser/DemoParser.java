import java.io.*;
import jason.asSyntax.parser.*;
import jason.asSemantics.*;

public class DemoParser {
    public static void main(String[] args) throws Exception {
        // create a parser from a file (it could a=be  any kind of stream)
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

        System.exit(0);
    }
}
