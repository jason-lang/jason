package test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.SimpleCharStream;
import jason.asSyntax.parser.Token;
import jason.asSyntax.parser.as2jTokenManager;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class VarTermTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    /** test when a var is ground with a Term or another var */
    public void testVarTermAsTerm() throws ParseException {
        Structure k = new VarTerm("K");
        Unifier u = new Unifier();
        u.unifies(k, new Structure("a1"));
        assertTrue("K".equals(k.toString()));
        k = (Structure)k.capply(u);
        assertTrue("a1".equals(k.toString()));
        k.addTerm(new Structure("p1"));
        k.addTerm(new Structure("p2"));
        assertEquals(k.getArity(), 2);

        VarTerm x1 = new VarTerm("X1");
        VarTerm x2 = new VarTerm("X2");
        VarTerm x3 = new VarTerm("X3");
        VarTerm x4 = new VarTerm("X4");
        VarTerm x5 = new VarTerm("X5");
        VarTerm x6 = new VarTerm("X6");

        VarTerm x7 = new VarTerm("X7");
        VarTerm x8 = new VarTerm("X8");
        VarTerm x9 = new VarTerm("X9");

        u = new Unifier();
        u.unifies(x1,x2);
        u.unifies(x3,x4);
        u.unifies(x4,x5);
        u.unifies(x2,x3);
        u.unifies(x1,x6);

        u.unifies(x7,x8);
        u.unifies(x9,x8);

        u.unifies(x7,x4);
        u.unifies(x3,new Structure("a"));
        assertEquals(u.get(x1).toString(),"a");
        assertEquals(u.get(x2).toString(),"a");
        assertEquals(u.get(x3).toString(),"a");
        assertEquals(u.get(x4).toString(),"a");
        assertEquals(u.get(x5).toString(),"a");
        assertEquals(u.get(x6).toString(),"a");
        assertEquals(u.get(x7).toString(),"a");
        assertEquals(u.get(x8).toString(),"a");
        assertEquals(u.get(x9).toString(),"a");

        assertEquals(x1.capply(u).toString(),"a");

        // unification with lists
        VarTerm v1 = new VarTerm("L");
        ListTerm lt = ListTermImpl.parseList("[a,B,a(B)]");
        u = new Unifier();
        u.unifies(new VarTerm("B"), new Structure("oi"));
        u.unifies(v1, lt); // L = [a,B,a(B)]
        //v1.apply(u);
        //ListTerm vlt = (ListTerm) v1.getValue();
        ListTerm vlt = (ListTerm) v1.capply(u);
        assertFalse(vlt.equals(lt)); // the apply in var should not change the original list
        Iterator<Term> i = vlt.iterator();
        i.next();
        i.next();
        Term third = i.next();
        Term toi1 = ASSyntax.parseTerm("a(oi)");
        Term toi2 = ASSyntax.parseTerm("a(B)");
        toi2 = toi2.capply(u);
        assertEquals(toi1,toi2);
        assertTrue(third.equals(toi1));
    }

    // test when a var is ground with a Pred
    public void testVarTermAsPred() {
        Literal k = new VarTerm("K");
        Unifier u = new Unifier();
        u.unifies(k, new Pred("p"));
        assertFalse(k.isPred());
        k = (Literal)k.capply(u);
        assertTrue(k.isPred());
        assertFalse(k.hasAnnot());
        k.addAnnot(new Structure("annot1"));
        assertTrue(k.hasAnnot());

        k.addSource(new Structure("marcos"));
        assertEquals(k.getAnnots().size(), 2);
        k.delSources();
        assertEquals(k.getAnnots().size(), 1);

        // test with var not ground
        k = new VarTerm("K");
        u = new Unifier();
        u.unifies(k, Pred.parsePred("p[a]"));
        k.addAnnot(new Structure("annot1"));
        k.addAnnot(new Structure("annot2"));
        assertEquals(k.getAnnots().size(), 2);
    }

    // test when a var is ground with a Literal
    public void testVarTermAsLiteral() {
        Literal k = new VarTerm("K");
        Unifier u = new Unifier();
        assertTrue(k.isVar());
        Literal l = Literal.parseLiteral("~p(a1,a2)[n1,n2]");
        assertTrue(l.isLiteral());
        assertTrue(l.isPred());
        assertTrue(l.negated());
        assertTrue(u.unifies(k, l));
        assertFalse(k.isLiteral());
        k = (Literal)k.capply(u);
        // System.out.println(k+" u="+u);
        assertFalse(k.isVar());
        assertTrue(k.isLiteral());
        assertTrue(k.negated());
    }

    // test when a var is ground with a List
    public void testVarTermAsList() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        Term l1 = (Term) ListTermImpl.parseList("[a,b,c]");
        assertTrue(l1.isList());
        assertTrue(u.unifies(k, l1));
        assertFalse(k.isList());
        // u.apply(k);
        // assertTrue(k.isList());
        // assertEquals(k.size(),3);

        ListTerm l2 = ListTermImpl.parseList("[d,e|K]");
        // System.out.println("l2="+l2);
        Literal nl = new VarTerm("NK");
        u.unifies(nl, (Term) l2);
        nl = (Literal)nl.capply(u);
        // System.out.println(nl+ " un="+u);
        assertEquals(((ListTerm)nl).size(), 5);

        l2 = (ListTerm)l2.capply(u);
        assertEquals(l2.size(), 5);
        assertEquals(l2.toString(), "[d,e,a,b,c]");
    }

    // test when a var is ground with a Number
    public void testVarTermAsNumber() throws Exception {
        Term k = new VarTerm("K");
        Unifier u = new Unifier();
        NumberTermImpl n = new NumberTermImpl(10);
        assertTrue(n.isNumeric());
        assertFalse(n.isVar());
        assertTrue(u.unifies(k, n));
        k = k.capply(u);
        // System.out.println(k+" u="+u);
        assertTrue(k.isNumeric());
        assertFalse(k.isLiteral());

        ArithExpr exp = new ArithExpr((NumberTerm)k, ArithmeticOp.plus, new NumberTermImpl(20));
        assertTrue(exp.solve() == 30d);
        NumberTerm nt = ArithExpr.parseExpr("5 div 2");
        assertTrue(nt.solve() == 2d);
        nt = ArithExpr.parseExpr("5 mod 2");
        assertTrue(nt.solve() == 1d);
    }

    public void testUnify() throws ParseException {
        // var with literal
        VarTerm k = new VarTerm("K");
        Literal l1 = Literal.parseLiteral("~p(a1,a2)[n1,n2]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(k, l1));
        assertTrue(k.isVar());
        assertTrue(u.unifies(l1, k));

        k = new VarTerm("K");
        Literal l2 = Literal.parseLiteral("p(a1,a2)[n1,n2]");
        u = new Unifier();
        assertTrue(u.unifies(k, l1));
        // System.out.println(k+" - "+u);
        assertFalse(u.unifies(l2, k));

        Literal l3 = Literal.parseLiteral("~p(X,Y)[A1]");
        VarTerm k2 = new VarTerm("K");
        u = new Unifier();
        assertTrue(u.unifies(k2, l3));
        assertTrue(u.unifies(k2, l1));

        VarTerm v1 = ASSyntax.parseVar("Y[b(2)]");
        VarTerm v2 = ASSyntax.parseVar("X");
        u.clear();
        u.unifies(v2, Pred.parsePred("a(4)[b(2)]"));
        u.unifies(v1, v2);
        VarTerm vy = new VarTerm("Y");
        // Y[b(2)] = Y
        assertEquals(v1.hashCode(),vy.hashCode());
    }

    public void testVarWithAnnots1() throws ParseException {
        VarTerm v1 = ASSyntax.parseVar("X[a,b,c]");
        VarTerm v2 = ASSyntax.parseVar("X[a,b]");
        assertTrue(v1.equals(v2));
        v2.addAnnot(new Structure("c"));
        assertTrue(v1.equals(v2));
        assertTrue(v2.equals(v1));

        Unifier u = new Unifier();
        Pred p1 = Pred.parsePred("p(t1,t2)[a,c]");
        // X[a,b,c] = p[a,c] nok
        assertFalse(u.unifies(v1, p1));
        assertEquals("p(t1,t2)[a,c]",p1.toString());

        // p[a,c] = X[a,b,c] ok (X is p)
        assertTrue(u.unifies(p1, v1));
        assertEquals("p(t1,t2)[a,c]",p1.toString());
        assertEquals(u.get("X").toString(), "p(t1,t2)");

        p1.addAnnot(new Structure("b"));
        p1.addAnnot(new Structure("d"));
        u.clear();
        // p[a,c,b,d] = X[a,b,c] nok
        assertFalse(u.unifies(p1, v1));

        u.clear();
        // X[a,b,c] = p[a,c,b,d] ok (X is p)
        assertTrue(u.unifies(v1, p1));
        assertEquals(u.get("X").toString(), "p(t1,t2)");
        assertEquals("p(t1,t2)[a,b,c]",v1.capply(u).toString());
    }

    public void testVarWithAnnots2() throws ParseException {
        // test vars annots

        // X[a] = Y[a,b] - ok
        VarTerm v1 = ASSyntax.parseVar("X[a]");
        VarTerm v2 = ASSyntax.parseVar("Y[a,b]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(v1, v2));

        // X[a,b] = Y[a] - not ok
        u = new Unifier();
        assertFalse(u.unifies(v2, v1));

        assertTrue(u.unifies(v1, v2));
        assertTrue(u.unifies(new LiteralImpl("vvv"), v1));
        assertEquals("vvv[a]", v1.capply(u).toString());
    }

    public void testVarWithAnnots3() throws ParseException {
        // X[a,b,c,d] = Y[a,c|R] - ok and R=[b,d]
        VarTerm v1 = ASSyntax.parseVar("X[a,b,c,d]");
        VarTerm v2 = ASSyntax.parseVar("Y[a,c|R]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(v1, v2));
        assertEquals("[b,d]",u.get("R").toString());
    }

    public void testVarWithAnnots4() throws ParseException {
        // X[source(A)] = open[source(a)] - ok and A -> a
        VarTerm v1 = ASSyntax.parseVar("X[source(A)]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(v1, Literal.parseLiteral("open[source(a)]")));
        assertEquals(u.get("A").toString(),"a");
        assertEquals(u.get("X").toString(),"open");

        VarTerm v2 = ASSyntax.parseVar("X[source(self)]");
        u = new Unifier();
        assertFalse(u.unifies(v2, Literal.parseLiteral("open[source(a)]")));
    }

    public void testVarWithAnnots5() throws ParseException {
        // X[A|R] = p(1)[a,b,c] - ok and
        // X = p(1), A = a, R=[b,c]
        VarTerm v = ASSyntax.parseVar("X[A|R]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(v, Literal.parseLiteral("p(1)[a,b,c]")));
        assertEquals("[b,c]", u.get("R").toString());
        assertEquals("a", u.get("A").toString());
        assertEquals("p(1)", u.get(v).toString());
        assertEquals("p(1)[a,b,c]", v.capply(u).toString());
    }


    public void testVarWithAnnots6() throws ParseException {
        // P -> open[source(a)]
        // P[source(self)]
        // apply on P is open[source(a),source(self)]?
        Unifier u = new Unifier();
        u.unifies(new VarTerm("P"), Literal.parseLiteral("open[source(a)]"));
        VarTerm v1 = ASSyntax.parseVar("P[source(self)]");
        Literal tv1 = (Literal)v1.capply(u);
        assertEquals(2,tv1.getAnnots().size());
    }

    public void testVarWithAnnotsInLogCons() throws RevisionFailedException, ParseException {
        Agent ag = new Agent();
        ag.initAg();

        ag.addBel(Literal.parseLiteral("b1[b]"));
        ag.addBel(Literal.parseLiteral("b2[d]"));

        Unifier u = new Unifier();
        VarTerm v1 = ASSyntax.parseVar("P[d]");
        assertEquals(2, iteratorSize(ag.getBB().getCandidateBeliefs(v1, new Unifier())));
        Iterator<Unifier> i = v1.logicalConsequence(ag, u);
        assertTrue(i.hasNext());
        u = i.next(); // u = {P[d]=b2}
        assertEquals("b2[d]",v1.capply(u).toString());
    }

    @SuppressWarnings("unchecked")
    private int iteratorSize(Iterator i) {
        int c = 0;
        while (i.hasNext()) {
            i.next();
            c++;
        }
        return c;
    }

    public void testSimple1() {
        Term um = new NumberTermImpl(1);
        Term dois = new NumberTermImpl(2);
        Term exp = ArithExpr.parse("X+1");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new NumberTermImpl(1));
        // X+1 not unifies with 1
        exp = exp.capply(u);
        assertFalse(u.unifies(exp, um));
        // X+1 unifies with 2
        assertTrue(u.unifies(exp, dois));
    }

    public void testSimple2() throws ParseException {
        VarTerm v = new VarTerm("X");
        assertFalse(v.isAtom());
        assertTrue(v.isVar());
        Term t;

        as2jTokenManager tokens = new as2jTokenManager(new SimpleCharStream(new StringReader("Event")));
        Token tk = tokens.getNextToken();
        assertEquals(tk.kind, jason.asSyntax.parser.as2jConstants.VAR);

        t = ASSyntax.parseVar("Ea");
        assertFalse(t.isAtom());
        assertTrue(t.isVar());

        t = ASSyntax.parseTerm("Event");
        assertFalse(t.isAtom());
        assertTrue(t.isVar());

    }

    public void testUnify1() throws ParseException {
        Term a1 = ASSyntax.parseTerm("s(1,2)");
        Term a2 = ASSyntax.parseTerm("s(X1,X2)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(new VarTerm("X1"),new VarTerm("X3")));
        assertTrue(u.unifies(a1,a2));
        assertEquals(u.get("X3").toString(),"1");
    }

    public void testUnify2() throws ParseException {
        Term a1 = ASSyntax.parseTerm("~X");
        Term a2 = ASSyntax.parseTerm("~s");
        Unifier u = new Unifier();
        assertTrue(u.unifies(a1,a2));
        assertEquals("s",u.get("X").toString());
    }

    public void testInnerVarUnif() {
        Unifier u = new Unifier();
        Literal l = Literal.parseLiteral("op(X)");
        u.unifies(new VarTerm("M"), l);
        u.unifies(new VarTerm("M"), Literal.parseLiteral("op(1)"));
        //assertEquals(u.get("M").toString(),"op(1)");
        assertEquals(l.capply(u).toString(),"op(1)");
    }

    public void testUnnamedVar1() throws ParseException {
        Term a1 = ASSyntax.parseTerm("a(_,_)");
        Term a2 = ASSyntax.parseTerm("a(10,20)");
        Term a3 = ASSyntax.parseTerm("a(30,40)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(a1,a2));
        assertFalse(u.unifies(a1,a3));
        assertEquals(a1.capply(u).toString(), ASSyntax.parseTerm("a(10,20)").toString());

        UnnamedVar v1 = new UnnamedVar();
        UnnamedVar v2 = (UnnamedVar)v1.clone();
        assertEquals(v1.toString(), v2.toString());

    }

    public void testUnnamedVar2() {
        Structure t1 = Structure.parse("a(Y)");
        assertFalse(t1.isGround());
        Structure t1c = (Structure)t1.clone();
        assertFalse(t1c.isGround());
        t1c.makeVarsAnnon();
        assertFalse(t1c.isGround());
        Term t1cc = (Term)t1c.clone();
        assertFalse(t1cc.isGround());

        Unifier u = new Unifier();
        VarTerm v = new VarTerm("X");
        assertTrue(v.isVar());
        u.unifies(v, t1cc);
        assertTrue(v.isVar());
        assertFalse(v.isGround());
        Term tv = v.capply(u);
        assertFalse(tv.isVar());
        assertFalse(tv.isGround());
    }

    public void testUnamedVarAnnots() throws ParseException {
        Term t = ASSyntax.parseTerm("_[scheme(Id)]");
        Map<VarTerm,Integer> c = new HashMap<VarTerm, Integer>();
        t.countVars(c);
        assertEquals(1,c.get(new VarTerm("Id")).intValue());
    }

    public void testUnifClone() {
        VarTerm x1 = new VarTerm("X");
        VarTerm x2 = new VarTerm("X");
        assertEquals(x1,x2);

        Unifier u1 = new Unifier();
        u1.unifies(x1, new VarTerm("Y"));
        u1.unifies(x2, new VarTerm("Z"));
        Unifier u2 = (Unifier)u1.clone();
        Object o1 = u1.get("X");
        Object o2 = u2.get("X");
        assertEquals(o1,o2);

        assertEquals(u1,u2);
    }

    public void testApply() {
        VarTerm x = new VarTerm("X");
        VarTerm y = new VarTerm("Y");

        Unifier u = new Unifier();

        // X = Y
        u.unifies(y, x);

        x = (VarTerm)x.clone();

        // Y = 10
        u.unifies(y, new NumberTermImpl(10));

        assertEquals(x.capply(u).toString(), "10");
    }

    public void testUnnamedvarsorder() {
        // the order is important for the "return" of plans/rules (where makeVarAnnon is used)
        // the most recently created unnamed vars should come last
        List<VarTerm> l = new ArrayList<VarTerm>();
        l.add(new UnnamedVar(5));
        l.add(new VarTerm("F"));
        l.add(new UnnamedVar(6));
        l.add(new UnnamedVar(11));
        l.add(new VarTerm("B"));
        Collections.sort(l);
        //assertEquals("[B, F, _11, _6, _5]", l.toString()); // this order is VERY important for unification!
        assertEquals("[B, F, _5, _6, _11]", l.toString()); // this order is VERY important for unification!

        VarTerm v1 = new UnnamedVar();
        VarTerm v2 = new UnnamedVar();
        assertTrue(v1.clone().compareTo(v2.clone()) < 0);
        assertTrue(v1.clone().compareTo(v1.clone()) == 0);
        assertTrue(v2.clone().compareTo(v1.clone()) > 0);
    }

    public void testCopy() {
        VarTerm x = new VarTerm("X");
        Unifier u = new Unifier();
        u.unifies(x, Literal.parseLiteral("goto(3,2)[source(bob)]"));
        assertEquals("goto(3,2)[source(bob)]", x.capply(u).toString());
    }

    public void testCompare() {
        Term x = new VarTerm("X");
        Term y = new VarTerm("Y");
        Unifier u = new Unifier();
        u.unifies(x, new NumberTermImpl(10));
        u.unifies(y, new NumberTermImpl(20));
        x = x.capply(u);
        y = y.capply(u);
        assertTrue(x.compareTo(y) < 0);
        assertTrue(y.compareTo(x) > 0);

        assertTrue(new StringTermImpl("z").compareTo(new VarTerm("X")) < 0);
        assertTrue(new VarTerm("X").compareTo(new StringTermImpl("z")) > 0);
        Term Z = new VarTerm("Z");
        u = new Unifier();
        u.unifies(Z, new StringTermImpl("z"));
        Z = Z.capply(u);
        assertFalse(Z.compareTo(new StringTermImpl("z")) > 0);
        assertFalse(Z.compareTo(new StringTermImpl("z")) < 0);
        assertFalse(new StringTermImpl("z").compareTo(Z) > 0);
        assertFalse(new StringTermImpl("z").compareTo(Z) < 0);
        assertTrue(new StringTermImpl("z").compareTo(Z) == 0);
        assertTrue(Z.compareTo(new StringTermImpl("z")) == 0);

        ListTerm l = ASSyntax.createList(new Atom("a"), new Atom("c"));
        assertTrue(l.compareTo(new VarTerm("X")) < 0);
        assertTrue(new VarTerm("X").compareTo(l) > 0);
        VarTerm L = new VarTerm("L");
        /*L.setValue(ASSyntax.createList(new Atom("a"), new Atom("b")));
        assertTrue(L.compareTo(l) < 0);
        assertTrue(l.compareTo(L) > 0);
        */
        // Tim Cleaver tests
        assertTrue(new NumberTermImpl(1).compareTo(new VarTerm("X")) < 0);
        assertTrue(new VarTerm("X").compareTo(new NumberTermImpl(1)) > 0);
        //VarTerm X = new VarTerm("X");
        //X.setValue(new NumberTermImpl(2));
        Term X = new NumberTermImpl(2);
        assertTrue(X.compareTo(new NumberTermImpl(1)) > 0);
        assertTrue(new NumberTermImpl(1).compareTo(X) < 0);
    }

    public void testUnifyNegVar() throws ParseException {
        Literal l1 = ASSyntax.parseLiteral("~B");
        Literal l2 = ASSyntax.parseLiteral("~p(1)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(l1, l2)); // ~B = ~p(1)
        assertEquals(u.get((VarTerm)l1).toString(),"p(1)");
        // apply in ~B should result in ~p(1)
        assertEquals(l1.capply(u).toString(),"~p(1)");

        VarTerm b = new VarTerm("B");
        assertEquals(b.capply(u).toString(),"p(1)");

        l1 = ASSyntax.parseLiteral("~B");
        l2 = ASSyntax.parseLiteral("p(1)");
        u = new Unifier();
        assertFalse(u.unifies(l1, l2));

        assertFalse(u.unifies(l1, ASSyntax.parseTerm("10")));

        l1 = ASSyntax.parseLiteral("B");
        l2 = ASSyntax.parseLiteral("~p(1)");
        u = new Unifier();
        assertTrue(u.unifies(l1, l2));
        assertEquals(u.get("B").toString(),"~p(1)");
        assertEquals(l1.capply(u).toString(),"~p(1)");

        l1 = ASSyntax.parseLiteral("~B");
        l2 = ASSyntax.parseLiteral("A");
        u = new Unifier();
        assertFalse(u.unifies(l1, l2));

        l1 = ASSyntax.parseLiteral("~B");
        l2 = ASSyntax.parseLiteral("~A");
        u = new Unifier();
        assertTrue(u.unifies(l1, l2));
        // if A = p(10), apply in ~B should be ~p(10).
        VarTerm va = new VarTerm("A");
        u.unifies(va,ASSyntax.parseLiteral("p(10)"));
        //va.apply(u);
        assertEquals(va.capply(u).toString(),"p(10)");
        //l1.apply(u);
        assertEquals(l1.capply(u).toString(),"~p(10)");
        //l2.apply(u);
        assertEquals(l2.capply(u).toString(),"~p(10)");

        l1 = ASSyntax.parseLiteral("~B");
        l2 = ASSyntax.parseLiteral("~A");
        u = new Unifier();
        assertTrue(u.unifies(l1, l2)); // A = B
        // if ~A = ~p(10), then B -> p(10).
        assertTrue(u.unifies(l2, ASSyntax.parseLiteral("~p(10)")));
        //l2.apply(u);
        assertEquals("~p(10)", l2.capply(u).toString());
        //l1.apply(u);
        assertEquals("~p(10)", l1.capply(u).toString()); // ~B is ~p(10)
        VarTerm vb = new VarTerm("B");
        //vb.apply(u);
        assertEquals("p(10)", vb.capply(u).toString());

        u = new Unifier();
        u.unifies(new VarTerm("A"),ASSyntax.parseLiteral("p(10)"));
        u.unifies(new VarTerm("B"),ASSyntax.parseLiteral("~p(10)"));
        assertFalse(u.unifies(new VarTerm("A"), new VarTerm("B")));

        l1 = ASSyntax.parseLiteral("~B");
        l2 = ASSyntax.parseLiteral("~A");
        assertFalse(u.unifies(l1, l2));

        u = new Unifier();
        u.unifies(new VarTerm("A"),ASSyntax.parseLiteral("p(10)"));
        assertFalse(u.unifies( ASSyntax.createVar(Literal.LNeg, "B"), new VarTerm("A")));

        u = new Unifier();
        u.unifies(new VarTerm("A"),ASSyntax.parseLiteral("~p(10)"));
        vb = ASSyntax.createVar(Literal.LNeg, "B");
        assertTrue(u.unifies(vb,new VarTerm("A")));
        //vb.apply(u);
        assertEquals(vb.capply(u).toString(),"~p(10)");

        u = new Unifier();
        u.unifies(new VarTerm("A"),ASSyntax.parseLiteral("~p(10)"));
        vb = ASSyntax.createVar(Literal.LNeg, "B");
        assertTrue(u.unifies(vb,new VarTerm("A")));
        vb = new VarTerm("B");
        //vb.apply(u);
        assertEquals(vb.capply(u).toString(),"p(10)");

        // B = ~A
        // A = p(10)
        // => apply B is ~p(10)
        //    apply A is p(10)
        //    apply ~A is ~p(10)
        /* should not work any more (B = ~A does not unify)
        l1 = ASSyntax.parseLiteral("B");
        l2 = ASSyntax.parseLiteral("~A");
        u = new Unifier();
        assertTrue(u.unifies(l1, l2));
        va = new VarTerm("A");
        u.unifies(va,ASSyntax.parseLiteral("p(10)"));
        //va.apply(u);
        assertEquals(va.capply(u).toString(),"p(10)");
        //l1.apply(u);
        assertEquals(l1.capply(u).toString(),"~p(10)");
        //l2.apply(u);
        assertEquals(l2.capply(u).toString(),"~p(10)");
        */
    }

}
