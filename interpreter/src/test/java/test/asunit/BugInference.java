package test.asunit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import jason.asunit.TestAgent;

/** based on bug found by Cranefield */
public class BugInference {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "applicable_plans([], []). "+ // :- .print(\"applicable_plans/2 recursion finished successfully\").\n" +
            "applicable_plans([Plan|Plans], [Plan|APlans]) :-\n" +
            "    Plan = plan(_,_,Context,_) &\n" +
            "    Context &\n" +
            //"    .print(\"Plan\", Plan, \" with context \", Context, \" is **applicable**\") &\n" +
            "    applicable_plans(Plans, APlans).\n" +
            "applicable_plans([Plan|Plans], APlans) :-\n" +
            "    Plan = plan(_,_,Context,_) &\n" +
            "    not Context &\n" +
            //"    .print(\"Plan\", Plan, \" with context \", Context, \" is **NOT applicable**\") &\n" +
            "    applicable_plans(Plans, APlans).\n" +
            "\n" +
            "is_believed.\n" +
            "\n" +
            "+!g : is_believed <- irrelevant_action.\n" +
            "+!g : is_not_believed <- irrelevant_action2.\n" +
            "\n" +
            "+!test <-\n" +
            "    .relevant_plans({+!g}, RPlans);\n" +
            //"    .print(\"Relevant plans for +!g are: \", RPlans);\n" +
            "    ?applicable_plans(RPlans, APlans);\n" +
            "    jason.asunit.print(APlans)."
        );
    }

    @Test(timeout=4000)
    public void testGoal() {
        ag.addGoal("test");
        ag.assertPrint("+!g : is_believed <- irrelevant_action }]", 70);
    }

    @Test(timeout=2000)
    public void testUnif() throws ParseException {
        Literal l1 = ASSyntax.parseLiteral("applicable_plans(RPlans,APlans)");
        Literal l2 = ASSyntax.parseLiteral("applicable_plans([{ @l__1 +!g : is_believed <- irrelevant_action }],[{ @l__1 +!g : is_believed <- irrelevant_action }])[source(self)]");
        Unifier u = new Unifier();
        Assert.assertTrue( u.unifies(l1, l2));
    }

}
