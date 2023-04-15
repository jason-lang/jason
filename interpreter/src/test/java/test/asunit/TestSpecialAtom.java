package test.asunit;

import org.junit.Before;
import org.junit.Test;

import jason.asunit.TestAgent;

public class TestSpecialAtom {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        ag.setDebugMode(true);

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!t  <- .term2string(foo('1he%%llo'),S);\n" +
            "      jason.asunit.print(S); " +
            "      .term2string(T,S); " +
            "      .type(T,TOT); jason.asunit.print(TOT); " +
            "      T =.. [F,[Term|_],Annots]; " +
            "      jason.asunit.print(F,\" \",Term,\" \"); " +
            "      .type(Term,TTerm); jason.asunit.print(TTerm)."
        );
    }

    @Test(timeout=2000)
    public void testTypes() {
        ag.addGoal("t");
        ag.assertPrint("foo('1he%%llo')", 5);
        ag.assertPrint("literal", 5);
        ag.assertPrint("foo '1he%%llo' ", 5);
        ag.assertPrint("atom", 5);
    }

}
