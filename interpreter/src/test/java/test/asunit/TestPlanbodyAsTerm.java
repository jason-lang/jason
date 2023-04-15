package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestPlanbodyAsTerm {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "+!start <- +g( {a(1); b; c}); ?g(X); !g(X). " +
            "+!test2 <- !g( {!g2(1)}). "+
            "+!test3 <- !g2(-1 + 2)."+
            "+!test4 <- X = {a(1); b; c}; !g(X)."+

            "+!test5 <- !g5(R); jason.asunit.print(R). "+
            "+!g5(X) <- C; .print(errrrrror). "+
            "-!g5(X)[error(I),error_msg(M)] <- jason.asunit.print(I,M); X = ok. "+

            "+!test6 <- A = { a }; B = { A }; C = { B }; C; jason.asunit.print(end). "+

            "+!test7(X) <- A = { jason.asunit.print(a,X); jason.asunit.print(b,X*2) }; A; jason.asunit.print(end,X/2). "+

            "+!g({A; R}) <- A; !g(R). "+
            "+!g(A)    <- A." +
            "+!g2(A)     <- jason.asunit.print(p,A,p)."+

            "+!trl <- !myadd( { jason.asunit.print(a); jason.asunit.print(b) } ); !grl. "+
            "+!myadd(Action) <- +>{+!grl : c <- Action}; +>{+!grl <- jason.asunit.print(ops) }. \n" +

            "+!trl2 <- !myadd2( { jason.asunit.print(c2); jason.asunit.print(d2) } ); !grl2(20). "+
            "+!myadd2(Action) <- Plan =.. [ mynewlabel2, {+!grl2(C)}, {C > 10}, Action]; .add_plan(Plan). \n" +

            "@mylabel +!plan1(1) : 1 > 10 <- .print(a); c(1); !g2(bob). \n"+
            "+!testPTUn <- .relevant_plans({+!plan1(_)}, [Plan|_]); Plan = {@L +!T : C <- B}; B={BH;BT}; jason.asunit.print(L,T,C,BH,BT). \n"+

            "+!testPTUnLitBuild <- .relevant_plans({+!plan1(_)}, [Plan|_]); Plan =.. [L,T,C,B]; jason.asunit.print(L,T,C,B); !show_body(B). " +
            "+!show_body({})    <-  jason.asunit.print(\".\"). " +
            "+!show_body({H;T}) <-  jason.asunit.print(H); !show_body(T). "+

            //"tree_for(action(A)) :- A =.. [_, _, Annots] & jason.asunit.print(Annots). "+
            "tree_for(action(A[|Annots])) :- jason.asunit.print(Annots). "+
            "+!an1(A) : tree_for(action(A))<- jason.asunit.print(end). "

        );
    }

    @Test(timeout=2000)
    public void testAnnots1() {
        ag.addGoal("an1(p[a,b,c])");
        ag.assertPrint("[a,b,c]", 5);
    }

    @Test(timeout=2000)
    public void testProgram1a() {
        ag.addGoal("start");
        ag.assertBel("g({a(1);b;c})", 5);
        ag.assertAct("a(1)", 5);
        ag.assertAct("b", 4);
        ag.assertAct("c", 4);
    }

    @Test(timeout=2000)
    public void testProgram1b() {
        ag.addGoal("test4");
        ag.assertAct("a(1)", 4);
        ag.assertAct("b", 4);
        ag.assertAct("c", 4);
    }

    @Test(timeout=2000)
    public void testProgram2() {
        ag.addGoal("test2");
        ag.assertPrint("1", 5);
    }

    @Test(timeout=2000)
    public void testProgram3() {
        ag.addGoal("test3");
        ag.assertPrint("1", 5);
    }

    @Test(timeout=2000)
    public void test5() {
        ag.addGoal("test5");
        ag.assertPrint("body_var_without_valuestringcode:1: Variable 'C' must be ground.",10);
        ag.assertPrint("ok", 10);
    }

    @Test(timeout=2000)
    public void test6() {
        ag.addGoal("test6");
        ag.assertAct("a", 5);
        ag.assertPrint("end", 5);
    }

    @Test(timeout=2000)
    public void test7() {
        ag.addGoal("test7(100)");
        ag.assertPrint("a100", 5);
        ag.assertPrint("b200", 5);
        ag.assertPrint("end50", 5);
    }

    @Test(timeout=2000)
    public void test8() {
        ag.addGoal("trl");
        ag.assertPrint("ops", 5);
        ag.addBel("c");
        ag.addGoal("trl");
        ag.assertPrint("a", 5);
        ag.assertPrint("b", 5);
    }

    @Test(timeout=2000)
    public void test8bis() {
        ag.addGoal("trl2");
        ag.assertPrint("c2", 15);
        ag.assertPrint("d2", 15);
    }

    @Test(timeout=2000)
    public void testPlanUnif1() {
        ag.addGoal("testPTUn");
        ag.assertPrint("mylabel[source(self),url(\"stringcode\")]plan1(1)(1 > 10).print(a)c(1); !g2(bob)", 5);
    }

    @Test(timeout=2000)
    public void testPlanUnif2() {
        ag.addGoal("testPTUnLitBuild");
        ag.assertPrint("mylabel[source(self),url(\"stringcode\")]+!plan1(1)(1 > 10).print(a); c(1); !g2(bob)", 5);
        ag.clearExecutionTrace();
        ag.assertPrint(".print(a)", 15);
        ag.assertPrint("c(1)", 15);
        ag.assertPrint("!g2(bob)", 15);
        ag.clearExecutionTrace();
        ag.assertPrint(".", 15);
    }
}
