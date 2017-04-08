package test.asunit;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestIAdelete {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "pos(3). pos(4). pos(1). pos(10). "+
            "+!test1 <- .findall(pos(X),pos(X),L); !find_closest(5,L,Alloc,Rest); jason.asunit.print(\"*\",Alloc); jason.asunit.print(Rest)."+

            "+!test2 <- .findall(pos(X),pos(X),L); !alloc([1,2,3,4],L)."+

            "+!alloc([],_). "+
            "+!alloc([A|T],Options) <- !find_closest(A,Options,Loc,Rest); jason.asunit.print(A,\" to \",Loc); !alloc(T,Rest). " +

            "+!find_closest(Ag,Options, MinDist, Rest) <- "+
            "   ?calc_distances(Options,Distances,Ag); jason.asunit.print(Distances); "+
            "   .min(Distances,d(_,MinDist)); "+
            "   .delete(pos(MinDist),Options,Rest). "+

            "calc_distances([],[],_) :- true. "+
            "calc_distances([pos(F)|TP], [d(D,F)|TD], Ref) "+
            ":- D = math.abs(Ref - F) & calc_distances(TP, TD, Ref). "
        );
    }

    @Test(timeout=2000)
    public void testDelete() {
        ag.addGoal("test1");
        ag.assertPrint("[d(2,3),d(1,4),d(4,1),d(5,10)]", 5);
        ag.assertPrint("*4", 5);
        ag.assertPrint("[pos(3),pos(1),pos(10)]", 3);
    }

    @Test(timeout=2000)
    public void testAlloc() {
        ag.addGoal("test2");
        ag.assertPrint("1 to 1", 10);
        ag.assertPrint("2 to 3", 10);
        ag.assertPrint("3 to 4", 10);
        ag.assertPrint("4 to 10", 10);
    }
}
