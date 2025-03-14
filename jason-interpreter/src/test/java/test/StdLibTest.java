package test;

import java.util.Iterator;

import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.IMCondition;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.stdlib.add_annot;
import jason.stdlib.add_nested_source;
import jason.stdlib.add_plan;
import jason.stdlib.fail_goal;
import jason.stdlib.relevant_plans;
import jason.stdlib.remove_plan;
import jason.stdlib.succeed_goal;
import junit.framework.TestCase;

/** JUnit test case for stdlib package */
public class StdLibTest extends TestCase {

    Intention intention1 = new Intention();
    Plan p4, p5;
    Agent ag;

    protected void setUp() throws Exception {
        super.setUp();

        intention1 = new Intention();
        Plan p = Plan.parse("+!g0 : true <- !g1; !g4.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier()), null));

        p = Plan.parse("+!g1 : true <- !g2.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier()), null));

        p = Plan.parse("+!g2 : true <- !g4; f;g.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier()), null));

        p4 = Plan.parse("+!g4 : true <- h.");
        intention1.push(new IntendedMeans(new Option(p4,new Unifier()), null));

        p5 = Plan.parse("+!g5 : true <- i.");

        ag = new Agent();
        ag.initAg();
        ag.getPL().add(Plan.parse("-!g1 : true <- j."));
    }

    public void testAddAnnot() throws ParseException {
        add_annot aa = new add_annot();
        Unifier u = new Unifier();

        Literal msg = Literal.parseLiteral("ok(10)");
        VarTerm X = new VarTerm("X");
        Term annot = ASSyntax.parseTerm("source(jomi)");
        try {
            aa.execute(null, u, new Term[] { msg, annot, X });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(msg.toString(), "ok(10)");
        assertTrue(((Pred) u.get("X")).hasAnnot(annot));

        // testing addAnnot with list
        ListTerm msgL = ASSyntax.parseList("[a,ok(10),[ok(20),ok(30),[ok(40)|[ok(50),ok(60)]]]]");
        VarTerm Y = new VarTerm("Y");
        Term annotL = ASSyntax.parseTerm("source(rafa)");
        assertEquals(msgL.toString(), "[a,ok(10),[ok(20),ok(30),[ok(40),ok(50),ok(60)]]]");
        try {
            aa.execute(null, u, new Term[] { msgL, annotL, Y });
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals("[a[source(rafa)],ok(10)[source(rafa)],[ok(20)[source(rafa)],ok(30)[source(rafa)],[ok(40)[source(rafa)],ok(50)[source(rafa)],ok(60)[source(rafa)]]]]",
                     (u.get("Y")).toString());
    }

    public void testAddNestedSource() throws Exception {
        add_nested_source aa = new add_nested_source();
        Unifier u = new Unifier();
        VarTerm x = new VarTerm("X");
        u.unifies(x, new Atom("a"));
        Term annot = ASSyntax.parseTerm("jomi");
        VarTerm r = new VarTerm("R");
        aa.execute(null, u, new Term[] { x.capply(u), annot, r });
        assertEquals("a[source(jomi)]", r.capply(u).toString());
        Term t = ASSyntax.parseTerm(r.capply(u).toString());
        Term s = ASSyntax.parseTerm("bob");
        r = new VarTerm("R");
        u = new Unifier();
        aa.execute(null, u, new Term[] { t, s, r });
        assertEquals("a[source(bob)[source(jomi)]]", r.capply(u).toString());

    }

    public void testFindAll() throws RevisionFailedException, ParseException {
        Agent ag = new Agent();
        ag.initAg();

        Literal l1 = Literal.parseLiteral("a(10,x)");
        assertFalse(l1.hasSource());
        ag.addBel(l1);
        ag.addBel(ASSyntax.parseLiteral("a(20,y)"));
        ag.addBel(ASSyntax.parseLiteral("a(30,x)"));
        assertEquals(ag.getBB().size(),3);

        Unifier u = new Unifier();
        Term X = ASSyntax.parseTerm("f(X)");
        Literal c = ASSyntax.parseLiteral("a(X,x)");
        c.addAnnot(BeliefBase.TSelf);
        VarTerm L = new VarTerm("L");
        // System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
        try {
            assertTrue((Boolean)new jason.stdlib.findall().execute(ag.getTS(), u, new Term[] { X, c, L }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ListTerm lt = (ListTerm) u.get("L");
        //System.out.println("found=" + lt);
        assertEquals(lt.size(), 2);
    }

    public void testGetRelevantPlansAndAddPlan() throws Exception {
        Agent ag = new Agent();
        ag.initAg();
        Plan pa = ASSyntax.parsePlan("@t1 +a : g(10) <- .print(\"ok 10\").");
        ag.getPL().add(pa, null, false);
        assertNotNull(pa);
        assertEquals("@t1[source(self)] +a : g(10) <- .print(\"ok 10\").", pa.toASString());

        ag.getPL().add(ASSyntax.parsePlan("@t2 +a : g(20) <- .print(\"ok 20\")."), new Structure("nosource"), false);
        ag.getPL().getPlans().get(1).addSource(new Structure("ag1"));
        ag.getPL().add(ASSyntax.parsePlan("@t3 +b : true <- true."), null, false);
        //System.out.println(ag.getPL());
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);

        Unifier u = new Unifier();
        StringTerm ste = new StringTermImpl("+a");
        VarTerm X = new VarTerm("X");
        //System.out.println(ag.getPL().getAllRelevant(Trigger.parseTrigger(ste.getFunctor()).getPredicateIndicator()));
        new relevant_plans().execute(ts, u, new Term[] { ste, X });
        assertEquals(ag.getPL().getPlans().get(0), pa);

        ListTerm plans = (ListTerm) u.get("X");
        //System.out.println("plans="+plans);

        assertEquals(plans.size(), 2);

        assertEquals(ag.getPL().getPlans().size(), 3);
        // remove plan t1 from PS
        new remove_plan().execute(ts, new Unifier(), new Term[] { new Pred("t1") });
        // ag.getPS().remove(0);
        assertEquals(ag.getPL().getPlans().size(), 2);

        // add plans returned from getRelevantPlans
        // using IA addPlan
        for (Term t : plans) {
            new add_plan().execute(ts, new Unifier(), new Term[]{t, new Structure("fromGR")});
        }

        // add again plans returned from getRelevantPlans
        // using IA addPlan receiving a list of plans
        new add_plan().execute(ts, new Unifier(), new Term[] { plans, new Structure("fromLT") });

        // the plan t2 (first plan now) must have 4 sources
        assertEquals(4, ag.getPL().get("t2").getSources().size());

        // the plan t1 (third plan now) must have 2 sources
        assertEquals(3, ag.getPL().get("t1").getSources().size());

        // remove plan t2,t3 (source = nosource) from PS
        ListTerm llt = ListTermImpl.parseList("[t2,t3]");
        assertTrue((Boolean)new remove_plan().execute(ts, new Unifier(), new Term[] { llt, new Pred("nosource") }));
        assertEquals(3, ag.getPL().getPlans().size());

        // remove plan t2,t3 (source = self) from PS
        llt = ListTermImpl.parseList("[t2,t3]");
        assertTrue((Boolean)new remove_plan().execute(ts, new Unifier(), new Term[] { llt }));
        assertEquals(2, ag.getPL().getPlans().size());

        // the plan t2 (first plan now) must have 3 sources
        assertEquals(3, ag.getPL().get("t2").getSources().size());

    }

    public void testConcat() {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l2 = ListTermImpl.parseList("[d,e,f]");
        ListTerm l3 = ListTermImpl.parseList("[a,b,c,d,e,f]");

        VarTerm X = new VarTerm("X");
        Unifier u = new Unifier();

        try {
            assertTrue((Boolean)new jason.stdlib.concat().execute(null, u, new Term[] { l1, l2, X }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(((ListTerm) u.get("X")).size(), 6);
        assertEquals(((ListTerm) u.get("X")), l3);

        l1 = ListTermImpl.parseList("[a,b,c]");
        l2 = ListTermImpl.parseList("[d,e,f]");
        l3 = ListTermImpl.parseList("[a,b,c,d,e,f]");

        try {
            assertTrue((Boolean)new jason.stdlib.concat().execute(null, new Unifier(), new Term[] { l1, l2, l3 }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSubString() throws Exception {
        StringTerm s1 = new StringTermImpl("a");
        StringTerm s2 = new StringTermImpl("bbacca");

        Term t1 = ASSyntax.parseTerm("a(10)");
        Term t2 = ASSyntax.parseTerm("[1,b(xxx,a(10))]");

        VarTerm X = new VarTerm("X");

        Unifier u = new Unifier();

        assertTrue((Boolean)new jason.stdlib.substring().execute(null, u, new Term[] { s1, s2 }));
        Iterator<Unifier> i = (Iterator)new jason.stdlib.substring().execute(null, u, new Term[] { s1, s2, X});
        assertEquals(i.next().get("X").toString(), "2");
        assertEquals(i.next().get("X").toString(), "5");
        assertFalse(i.hasNext());

        assertTrue((Boolean)new jason.stdlib.substring().execute(null, u, new Term[] { t1, t2}));
        i = (Iterator)new jason.stdlib.substring().execute(null, new Unifier(), new Term[] { t1, t2, X});
        assertTrue(i.hasNext());
        assertEquals(i.next().get("X").toString(), "9");
        assertFalse(i.hasNext());

    }

    public void testDropGoal1() throws ParseException {
        assertEquals(intention1.size(), 4);
        Trigger g = ASSyntax.parseTrigger("+!g1");
        assertNotNull(intention1.dropGoal(new IMCondition() {
            @Override
            public boolean test(Trigger t, Unifier u) {
                return u.unifies(t, g);
            }

            @Override
            public Trigger getTrigger() {
                return g;
            }
        }, new Unifier()));
        assertEquals(intention1.size(), 1);
    }

    public void testDropGoal2() throws Exception {
        Agent ag = new Agent();
        ag.initAg();
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);
        ts.getC().addRunningIntention(intention1);
        assertFalse(ts.hasGoalListener());
        new succeed_goal().drop(ts, Literal.parseLiteral("g2"), new Unifier());
        assertEquals(intention1.size(), 1);
        intention1.push(new IntendedMeans(new Option(p4,new Unifier()), null));
        new succeed_goal().drop(ts, Literal.parseLiteral("g4"), new Unifier());
        assertTrue(intention1.isFinished());
    }

    public void testDropGoal3() throws Exception {
        //Circumstance c = new Circumstance();
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);
        ts.getC().addRunningIntention(intention1);
        new fail_goal().drop(ts, Literal.parseLiteral("g2"), new Unifier());
        assertEquals(intention1.size(),2);
        assertEquals(1,ts.getC().getEvents().size());
    }

    @SuppressWarnings("unchecked")
    public void testMember() throws Exception {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        Term ta = ASSyntax.parseTerm("a");
        Term td = ASSyntax.parseTerm("d");

        // test member(a,[a,b,c])
        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { ta, l1});
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test member(d,[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { td, l1});
        assertFalse(i.hasNext());

        // test member(b(X),[a(1),b(2),c(3)])
        Term l2 = ASSyntax.parseTerm("[a(1),b(2),c(3)]");
        Term tb = ASSyntax.parseTerm("b(X)");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tb, l2});
        assertNotNull(i);
        assertTrue(i.hasNext());
        Unifier ru = i.next();
        assertEquals(0, u.size()); // u should not be changed!
        assertEquals(1, ru.size());
        assertEquals(ru.get("X").toString(), "2");

        // test member(X,[a,b,c])
        Term tx = ASSyntax.parseTerm("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tx, l1});
        assertEquals(3, iteratorSize(i));
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tx, l1});
        assertEquals(i.next().get("X").toString(),"a");
        assertEquals(i.next().get("X").toString(),"b");
        assertEquals(i.next().get("X").toString(),"c");
        assertFalse(i.hasNext());

        // test member(b(X),[a(1),b(2),c(3),b(4)])
        l2 = ASSyntax.parseTerm("[a(1),b(2),c(3),b(4)]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tb, l2});
        assertNotNull(i);
        assertEquals(2, iteratorSize(i));
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tb, l2});
        assertEquals(i.next().get("X").toString(),"2");
        assertEquals(i.next().get("X").toString(),"4");
    }

    @SuppressWarnings("unchecked")
    public void testPrefix() throws Exception {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l2 = ListTermImpl.parseList("[a,b]");
        ListTerm l3 = ListTermImpl.parseList("[b,c]");

        // test prefix([a,b,c],[a,b,c])
        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l1, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test prefix([a,b],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        //assertTrue(i.hasNext());
        //assertTrue(i.next().size() == 0);

        // test prefix([b,c],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l3, l1 });
        assertFalse(i.hasNext());

        // test prefix([a(X)],[a(1),b(2),c(3)])
        Term l4 = ASSyntax.parseTerm("[a(1),b(2),c(3)]");
        Term l5 = ASSyntax.parseTerm("[a(X)]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l5, l4 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        Unifier ru = i.next();
        assertEquals(0, u.size()); // u should not be changed!
        assertEquals(1, ru.size());
        assertEquals(ru.get("X").toString(), "1");

        // test prefix(X,[a,b,c])
        Term tx = ASSyntax.parseTerm("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { tx, l1 });
        assertEquals(4, iteratorSize(i));
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { tx, l1 });
        assertEquals(i.next().get("X").toString(),"[a,b,c]");
        assertEquals(i.next().get("X").toString(),"[a,b]");
        assertEquals(i.next().get("X").toString(),"[a]");
        assertEquals(i.next().get("X").toString(),"[]");
        assertFalse(i.hasNext());
    }

    @SuppressWarnings("unchecked")
    public void testPrefixString() throws Exception {
        Term l1 = ASSyntax.createString("abc");
        Term l2 = ASSyntax.createString("ab");
        Term l3 = ASSyntax.createString("bc");

        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l1, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test prefix([a,b],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        //assertTrue(i.hasNext());
        //assertTrue(i.next().size() == 0);

        // test prefix([b,c],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { l3, l1 });
        assertFalse(i.hasNext());

        // test prefix(X,[a,b,c])
        Term tx = ASSyntax.parseTerm("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { tx, l1 });
        assertEquals(4, iteratorSize(i));
        i = (Iterator<Unifier>)new jason.stdlib.prefix().execute(null, u, new Term[] { tx, l1 });
        assertEquals("\"abc\"", i.next().get("X").toString());
        assertEquals("\"ab\"",  i.next().get("X").toString());
        assertEquals("\"a\"",   i.next().get("X").toString());
        assertEquals("\"\"",    i.next().get("X").toString());
        assertFalse(i.hasNext());
    }


    @SuppressWarnings("unchecked")
    public void testSuffix() throws Exception {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l2 = ListTermImpl.parseList("[b,c]");
        ListTerm l3 = ListTermImpl.parseList("[a,b]");

        // test suffix([a,b,c],[a,b,c])
        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l1, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test suffix([b,c],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        //assertTrue(i.hasNext());
        //assertTrue(i.next().size() == 0);

        // test suffix([a,b],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l3, l1 });
        assertFalse(i.hasNext());

        // test suffix([c(X)],[a(1),b(2),c(3)])
        Term l4 = ASSyntax.parseTerm("[a(1),b(2),c(3)]");
        Term l5 = ASSyntax.parseTerm("[c(X)]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l5, l4 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        Unifier ru = i.next();
        assertEquals(0, u.size()); // u should not be changed!
        assertEquals(1, ru.size());
        assertEquals(ru.get("X").toString(), "3");

        // test suffix(X,[a,b,c])
        Term tx = ASSyntax.parseTerm("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { tx, l1 });
        assertEquals(4, iteratorSize(i));
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { tx, l1 });
        assertEquals(i.next().get("X").toString(),"[a,b,c]");
        assertEquals(i.next().get("X").toString(),"[b,c]");
        assertEquals(i.next().get("X").toString(),"[c]");
        assertEquals(i.next().get("X").toString(),"[]");
        assertFalse(i.hasNext());
    }

    @SuppressWarnings("unchecked")
    public void testSuffixString() throws Exception {
        Term l1 = ASSyntax.createString("abc");
        Term l2 = ASSyntax.createString("bc");
        Term l3 = ASSyntax.createString("ab");

        // test suffix([a,b,c],[a,b,c])
        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l1, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test suffix([b,c],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        //assertTrue(i.hasNext());
        //assertTrue(i.next().size() == 0);

        // test suffix([a,b],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { l3, l1 });
        assertFalse(i.hasNext());


        // test suffix(X,[a,b,c])
        Term tx = ASSyntax.parseTerm("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { tx, l1 });
        assertEquals(4, iteratorSize(i));
        i = (Iterator<Unifier>)new jason.stdlib.suffix().execute(null, u, new Term[] { tx, l1 });
        assertEquals("\"abc\"", i.next().get("X").toString());
        assertEquals("\"bc\"",  i.next().get("X").toString());
        assertEquals("\"c\"",   i.next().get("X").toString());
        assertEquals("\"\"",    i.next().get("X").toString());

        assertFalse(i.hasNext());
    }


    @SuppressWarnings("unchecked")
    public void testSublist() throws Exception {

        /* As for prefix */
        //
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l2 = ListTermImpl.parseList("[a,b]");
        ListTerm l3 = ListTermImpl.parseList("[b,c]");

        // test sublist([a,b,c],[a,b,c])
        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l1, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test sublist([a,b],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test sublist([a(X)],[a(1),b(2),c(3)])
        Term l4 = ASSyntax.parseTerm("[a(1),b(2),c(3)]");
        Term l5 = ASSyntax.parseTerm("[a(X)]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l5, l4 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        Unifier ru = i.next();
        assertEquals(0, u.size()); // u should not be changed!
        assertEquals(1, ru.size());
        assertEquals(ru.get("X").toString(), "1");

        /* As for suffix */
        //
        l1 = ListTermImpl.parseList("[a,b,c]");
        l2 = ListTermImpl.parseList("[b,c]");
        l3 = ListTermImpl.parseList("[a,b]");

        // test sublist([b,c],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test sublist([c(X)],[a(1),b(2),c(3)])
        l4 = ASSyntax.parseTerm("[a(1),b(2),c(3)]");
        l5 = ASSyntax.parseTerm("[c(X)]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l5, l4 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        ru = i.next();
        assertEquals(0, u.size()); // u should not be changed!
        assertEquals(1, ru.size());
        assertEquals(ru.get("X").toString(), "3");


        /* After passing prefix and suffix, test middle sublist (true and false) */
        //
        l1 = ListTermImpl.parseList("[a,b,c]");
        l2 = ListTermImpl.parseList("[b]");
        l3 = ListTermImpl.parseList("[d]");

        // test sublist([b],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l2, l1 });
        assertNotNull(i);
        assertTrue(i.hasNext());
        assertEquals(0, i.next().size());

        // test sublist([d],[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l3, l1 });
        assertFalse(i.hasNext());

        // test sublist([a,c],[a,b,c])
        l4 = ListTermImpl.parseList("[a,c]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { l4, l1 });
        assertFalse(i.hasNext());


        // Finally, test backtracking
        // test sublist(X,[a,b,c])
        Term tx = ASSyntax.parseTerm("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { tx, l1 });
        //assertTrue(iteratorSize(i) == 7);
        i = (Iterator<Unifier>)new jason.stdlib.sublist().execute(null, u, new Term[] { tx, l1 });
        assertEquals(i.next().get("X").toString(),"[a,b,c]");
        assertEquals(i.next().get("X").toString(),"[a,b]");
        assertEquals(i.next().get("X").toString(),"[a]");
        assertEquals(i.next().get("X").toString(),"[b,c]");
        assertEquals(i.next().get("X").toString(),"[b]");
        assertEquals(i.next().get("X").toString(),"[c]");
        assertEquals(i.next().get("X").toString(),"[]");
        assertFalse(i.hasNext());

    }



    public void testDelete() throws Exception {
        ListTerm l1 = ListTermImpl.parseList("[a,b,a,c,a]");
        Term ta = ASSyntax.parseTerm("a");
        VarTerm v = new VarTerm("X");

        // test delete(a,[a,b,a,c,a])
        Unifier u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { ta, l1, v}));
        assertEquals("[b,c]",u.get("X").toString());

        // test delete(3,[a,b,a,c,a])
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(3), l1, v}));
        assertEquals("[a,b,a,a]",u.get("X").toString());

        // test delete(3,"abaca")
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(3), new StringTermImpl("abaca"), v}));
        assertEquals("\"abaa\"",u.get("X").toString());

        // test delete("a","abaca")
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new StringTermImpl("a"), new StringTermImpl("abaca"), v}));
        assertEquals("\"bc\"",u.get("X").toString());

        // test delete(2,4,l1,L)
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(2), new NumberTermImpl(4), l1, v}));
        assertEquals("[a,b,a]",u.get("X").toString());
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(0), new NumberTermImpl(4), l1, v}));
        assertEquals("[a]",u.get("X").toString());

        // test delete(2,4,"abcdef")
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(2), new NumberTermImpl(4), new StringTermImpl("abcdef"), v}));
        assertEquals("\"abef\"",u.get("X").toString());

    }

    public void testPut() throws Exception {
        Agent ag = new Agent();
        ag.initAg();
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);

        Unifier u = new Unifier();
        u.unifies(ASSyntax.parseTerm("X"), ASSyntax.parseTerm("floripa"));
        u.bind(new UnnamedVar(26), ASSyntax.parseTerm("blabal"));
        assertTrue((Boolean)new jason.stdlib.puts().execute(ts, u, new Term[] { new StringTermImpl("Hello #{_26}, #{X}")}));
    }

    public void testReplace() throws Exception {
        Agent ag = new Agent();
        ag.initAg();
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);

        Unifier u = new Unifier();
        VarTerm y = new VarTerm("Y");
        assertTrue((Boolean)new jason.stdlib.replace().execute(ts, u,
            new Term[] {
                ASSyntax.createString("hello day"),
                ASSyntax.createString("day"),
                ASSyntax.createString("world"),
                y
            }
        ));
        assertEquals("\"hello world\"", u.get(y).toString());
    }

    public void testMin() throws RevisionFailedException, ParseException {
        Agent ag = new Agent();
        ag.initAg();

        ag.addBel(ASSyntax.parseLiteral("a(25)"));
        ag.addBel(ASSyntax.parseLiteral("a(20)"));
        ag.addBel(ASSyntax.parseLiteral("a(30)"));

        Unifier u = new Unifier();
        Term X = ASSyntax.parseTerm("X");
        Literal c = ASSyntax.parseLiteral("a(X)");
        VarTerm L = new VarTerm("L");
        try {
            assertTrue((Boolean)new jason.stdlib.min().execute(ag.getTS(), u, new Term[] { X, c, L }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals("20", u.get("L").toString());
    }

    @SuppressWarnings({ "rawtypes" })
    private int iteratorSize(Iterator i) {
        int c = 0;
        while (i.hasNext()) {
            i.next();
            c++;
        }
        return c;
    }

}
