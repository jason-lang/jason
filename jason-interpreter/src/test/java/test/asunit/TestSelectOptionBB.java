package test.asunit;

import jason.asunit.TestAgent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestSelectOptionBB {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "select__option(E,I,P,U) :- .print(\"option for \",E,\" and intention=\",I) & .print(P) & .print(U) & "+
                           "P = {@Label +!Trigger : Context <- Body} & .print(Label) & "+
                           "Label =.. [Functor, Terms, Annots] & .print(Annots) & "+
                           ".member(kk, Annots). "+

            "a(10). a(20). b(30). " +

            "+!g : a(X) <- jason.asunit.print(X). "+
            "@pt[kk] +!g : b(X) <- jason.asunit.print(X). "
        );
    }

    @Test(timeout=2000)
    public void testGC1() {
        ag.addGoal("g");
        ag.assertPrint("30", 10);
    }

}
