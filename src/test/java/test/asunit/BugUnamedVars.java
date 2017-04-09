package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bug reported by Tim Cleaver in jason-bugs */

public class BugUnamedVars {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "wrap([],[]). "+
            "wrap([_ | Rest], [wrapped(_) | Result]) :-  wrap(Rest, Result). "+

            "wrap2([], _). "+
            "wrap2([_ | Rest], Result) :- wrap2(Rest, Temp) & Result = wrapped(_, Temp). "+

            "+!start : wrap([a1,b1,c1],R) & R = [wrapped(a), wrapped(b), wrapped(c)] <- jason.asunit.print(ok)."+
            "+!start : wrap([a,b,c],R) & R = [wrapped(a), wrapped(a), wrapped(a)] <- jason.asunit.print(nok). " +
            "+!test2 : wrap2([1, 2, 3], Result) & Result = wrapped(1, wrapped(2, wrapped(3, 4))) <- jason.asunit.print(ok). "
        );
    }

    @Test(timeout=2000)
    public void testWrap1() {
        ag.addGoal("start");
        ag.assertPrint("ok", 20);
    }

    @Test(timeout=2000)
    public void testWrap2() {
        ag.addGoal("test2");
        ag.assertPrint("ok", 20);
    }

}
