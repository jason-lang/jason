package test;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithFunctionTerm;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Structure;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.runtime.Settings;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class TSTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testRelevant() throws ParseException, JasonException {
        Agent ag = new Agent();
        ag.initAg();
        ag.getPL().add(ASSyntax.parsePlan("@t1 +a(X) : g(10) <- .print(\"ok 10\")."), new Structure("nosource"), false);
        ag.getPL().add((Plan)ASSyntax.parseTerm("{ @t2 +a(X) : true <- .print(\"ok 20\") }"), new Structure("nosource"), false);
        ag.getPL().add(ASSyntax.parsePlan("@t3 +b : true <- true."), new Structure("nosource"), false);
        TransitionSystem ts = ag.getTS(); 
        Literal content = Literal.parseLiteral("~alliance");
        content.addSource(new Structure("ag1"));

        Trigger te1 = ASSyntax.parseTrigger("+a(10)");

        try {
            List<Option> rp = ts.relevantPlans(te1);
            // System.out.println("RP="+rp);
            assertEquals(rp.size(), 2);

            rp = ts.applicablePlans(rp);
            // System.out.println("AP="+rp);
            assertEquals(rp.size(), 1);

            // Option opt = ag.selectOption(rp);
            // IntendedMeans im = new IntendedMeans(opt);
            // System.out.println(im);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Trigger te2 = ASSyntax.parseTrigger("+a(20)");

        try {
            List<Option> rp = ts.relevantPlans(te2);
            // System.out.println("RP="+rp);
            assertEquals(rp.size(), 2);

            rp = ts.applicablePlans(rp);
            // System.out.println("AP="+rp);
            assertEquals(rp.size(), 1);

            //Option opt = ag.selectOption(rp);
            //IntendedMeans im = new IntendedMeans(opt);
            // System.out.println(im);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testIntentionOrder() {
        Intention i1 = new Intention();
        Intention i2 = new Intention(); 
        
        Intention i3 = new Intention(); 
        i3.setAtomic(1);
        assertTrue(i3.isAtomic());
        
        Intention i4 = new Intention();
        
        Queue<Intention> q1 = new PriorityQueue<Intention>();
        q1.offer(i1);
        q1.offer(i2);
        q1.offer(i3);
        q1.offer(i4);
        assertEquals(q1.poll().getId(), i3.getId());
        //System.out.println(q1.poll());
        //System.out.println(q1.poll());
        //System.out.println(q1.poll());

        /*
        List<Intention> l = new ArrayList<Intention>();
        l.add(i1);
        l.add(i2);
        l.add(i3);
        l.add(i4);
        Collections.sort(l);
        
        System.out.println(l);
        */
        
    }
    
    public void testCustomSelOp() {
        assertFalse(new Test1().hasCustomSelectOption());
        assertTrue(new Test2().hasCustomSelectOption());
    }
    
    class Test1 extends Agent {
        public void t() {}
    }
    class Test2 extends Agent {
        public Option selectOption(List<Option> options) {
            return super.selectOption(options);
        }
    }
    
    public void testAgentClone() throws Exception {
        Agent a = new Agent();

        a.initAg("examples/auction/ag3.asl");
        String p1 = a.getPL().toString();
        String b1 = a.getBB().toString();
        InternalAction ia1 = ((InternalActionLiteral)a.getPL().get("prop_alliance").getBody().getBodyTerm()).getIA(a);
        assertTrue(ia1 != null);
        // get the arith expr (B+C) from the plan
        Structure send1 = (Structure)a.getPL().get("palliance").getBody().getBodyNext().getBodyNext().getBodyTerm();
        ArithFunctionTerm add1  = (ArithFunctionTerm)((Structure)send1.getTerm(2)).getTerm(1);
        assertEquals("(B+C)", add1.toString());

        // the agent is null here because it is an arith expr
        //assertEquals(null, add1.getAgent());
        
        a = a.clone(new AgArch());
        assertEquals(p1, a.getPL().toString());
        assertEquals(b1.length(), a.getBB().toString().length());

        InternalAction ia2 = ((InternalActionLiteral)a.getPL().get("prop_alliance").getBody().getBodyTerm()).getIA(null);
        assertEquals(null, ia2); // the clone have to set null for the IA so they do not share the same implementation

        Structure send2 = (Structure)a.getPL().get("palliance").getBody().getBodyNext().getBodyNext().getBodyTerm();
        ArithFunctionTerm add2  = (ArithFunctionTerm)((Structure)send2.getTerm(2)).getTerm(1);
        assertEquals("(B+C)", add2.toString());

        // after clone, the agent in (B+C) must be the cloned agent
        assertTrue(a == add2.getAgent());
    }
    
}
