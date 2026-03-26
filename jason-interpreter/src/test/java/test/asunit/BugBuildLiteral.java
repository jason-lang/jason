package test.asunit;

import jason.asunit.TestAgent;
import org.junit.Before;
import org.junit.Test;

/** bug reported by driacats
 *
 * https://github.com/jason-lang/jason/issues/131
 */
public class BugBuildLiteral {

    TestAgent ag, oa, ob;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "b(10).\n" +
                    "!start.\n" +
                    "\n" +
                    "+!pa\n" +
                    "\t:\tb(10)\n" +
                    "\t<-\t.print( \"pa\" ).\n" +
                    "\n" +
                    "+!pb\n" +
                    "\t<-\t.print( \"pb\" ).\n" +
                    "\n" +
                    "+!start <- \n" +
                    //"\t\t.relevant_plans( {+!pa}, [PlanA|_] );\n" +
                    //"\t\tPlanA =.. ListOfPa;\n" +
                    //"\t\t.print( ListOfPa );\n" +
                    "\t\t.relevant_plans( {+!pb}, [PlanB|_] ); \n" +
                    "\t\tPlanB =.. ListOfPb;\n" +
                    "\t\tjason.asunit.print( ListOfPb )."
        );
    }

    @Test(timeout=2000)
    public void testProg() {
        ag.addGoal("start");
        ag.assertPrint("true,", 10);
    }

}
