package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestIF {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "b(3). " +
            "+!test1 <- a1;\n "+
            "           if( b(X)) { " +
            "              jason.asunit.print(X); " +
            "              b1; " +
            "           } else {" +
            "              jason.asunit.print(no); " +
            "           }\n "+
            "           a2.\n "+

            "+!test2 <- -b(_); !test1. "+

            "+!test3 <- if( b(X)) { Y = X*10; Z = 10; } else { Y = 60; Z=20; }; jason.asunit.print(Y,\" \",Z). "+
            "+!test4 <- -b(_); !test3. \n"+
            
            "+!testELIF <- if   (e(1)) { jason.asunit.print(1); } "+
            "              elif (e(2)) { jason.asunit.print(2); } "+
            "              elif (e(3)) { jason.asunit.print(3); } "+
            "              else        { jason.asunit.print(4); }. "
        );
        /*
        ag.parseAScode(
                "b(3). " +
                "+!test1 <- a1; "+
                "           if( b(X), {jason.asunit.print(X); b1}, {jason.asunit.print(no)}); "+
                "           a2. "+

                "+!test2 <- -b(_); !test1. "+

                "+!test3 <- if( b(X), { Y = X*10; Z = 10 }, { Y = 60; Z=20 }); jason.asunit.print(Y,\" \",Z). "+
                "+!test4 <- -b(_); !test3. "
        );
        */
    }

    @Test(timeout=2000)
    public void test1() {
        ag.addGoal("test1");
        ag.assertPrint("3", 5);
        ag.assertAct("b1", 5);
        ag.assertAct("a2", 5);
    }

    @Test(timeout=2000)
    public void test2() {
        ag.addGoal("test2");
        ag.assertPrint("no", 5);
        ag.assertAct("a2", 5);
    }

    @Test(timeout=2000)
    public void testUnifiyInThenElse() {
        ag.addGoal("test3");
        ag.assertPrint("30 10", 5);
        ag.addGoal("test4");
        ag.assertPrint("60 20", 10);
    }

    @Test(timeout=2000)
    public void tesElseIf() {
        ag.addBel("e(5)");
        ag.addGoal("testELIF");
        ag.assertPrint("4", 10);
        ag.addBel("e(3)");
        ag.addGoal("testELIF");
        ag.assertPrint("3", 10);
        ag.addBel("e(2)");
        ag.addGoal("testELIF");
        ag.assertPrint("2", 10);
        ag.addBel("e(1)");
        ag.addGoal("testELIF");
        ag.assertPrint("1", 10);
    }
}
