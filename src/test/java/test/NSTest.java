package test;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.directives.NameSpace;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class NSTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testPrint() throws ParseException {
        Term t1 = ASSyntax.parseLiteral("ns1::kk(30, j::uu)");
        assertEquals("ns1::kk(30,j::uu)", t1.toString());

        t1 = ASSyntax.parseLiteral("true");
        assertEquals("true", t1.toString());

        t1 = ASSyntax.parseLiteral("ns1::kk(30, A::uu)");
        assertEquals("ns1::kk(30,A::uu)", t1.toString());

        t1 = ASSyntax.parseLiteral("ns1::kk(30, j::A)");
        assertEquals("ns1::kk(30,j::A)", t1.toString());

        t1 = ASSyntax.parseLiteral("ns1::X");
        assertEquals("ns1::X", t1.toString());

        t1 = ASSyntax.parseLiteral("X::bob");
        assertEquals("X::bob", t1.toString());

        t1 = ASSyntax.parseLiteral("ns1::~X");
        assertEquals("ns1::~X", t1.toString());

        t1 = ASSyntax.parseLiteral("N::~X");
        assertEquals("N::~X", t1.toString());

        t1 = ASSyntax.parseLiteral("ns::_");
        assertTrue(t1.toString().startsWith("ns::_"));

        t1 = ASSyntax.parseLiteral("ns::~p");
        assertEquals("ns::~p", t1.toString());
    }

    public void testUnifies() throws ParseException {
        Term t1    = ASSyntax.parseLiteral("ns1::kk(30,j::uu)");
        Literal t2 = ASSyntax.parseLiteral("ns1::kk(30,j::uu)");
        Term t3 = ASSyntax.parseLiteral("kk(30,j::uu)");
        Term t4 = ASSyntax.parseLiteral("ns4::kk(30,j::uu)");

        assertTrue(new Unifier().unifies(t1, t2));

        t2 = ASSyntax.parseLiteral("ns1::kk(30,j::X)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(t1, t2));
        assertEquals("uu", u.get("X").toString());

        t2 = ASSyntax.parseLiteral("ns1::kk(30,j::_)");
        u = new Unifier();
        assertTrue(u.unifies(t1, t2));

        t2 = ASSyntax.parseLiteral("ns2::kk(30,X)");
        u = new Unifier();
        assertFalse(u.unifies(t1, t2));
        assertFalse(u.unifies(t1, t3));

        t2 = ASSyntax.parseLiteral("A::kk(30,X)");
        u = new Unifier();
        assertFalse(u.unifies(t1, t2)); // X \= j::uu

        t2 = ASSyntax.parseLiteral("A::kk(30,j::X)");
        u = new Unifier();
        assertTrue(u.unifies(t1, t2));
        assertEquals("ns1", u.get("A").toString());
        assertEquals("uu", u.get("X").toString());

        u = new Unifier();
        u.unifies(new VarTerm("A"), ASSyntax.parseTerm("default"));
        u.unifies(new VarTerm("X"), ASSyntax.parseTerm("uu"));
        t2 = (Literal)t2.capply(u);
        assertEquals("kk(30,j::uu)", t2.toString());

        assertTrue(u.unifies(t2, t3));
        assertTrue(u.unifies(t3, t2));
        assertFalse(u.unifies(t2, t1));
        assertFalse(u.unifies(t1, t2));
        assertFalse(u.unifies(t4, t1));
        assertFalse(u.unifies(t1, t4));
    }

    public void testUnifiesVarNS() throws ParseException {
        Literal p1 = ASSyntax.parseLiteral("ns::A");
        Literal p2 = ASSyntax.parseLiteral("ns::bob");
        Literal p3 = ASSyntax.parseLiteral("xx::bob");
        Literal p4 = ASSyntax.parseLiteral("bob");
        Literal p5 = ASSyntax.parseLiteral("B::bob");
        Literal p6 = ASSyntax.parseLiteral("ns::B");

        // ns::A = ns::bob // A -> bob
        Unifier u = new Unifier();
        assertTrue(u.unifies(p1, p2));
        assertEquals("bob", u.get("A").toString());

        // ns::A = xx::bob // fails
        u = new Unifier();
        assertFalse(u.unifies(p1, p3));
        assertFalse(u.unifies(p1, p4));

        // ns::A = B::bob  // A -> bob; B -> ns
        u = new Unifier();
        assertTrue(u.unifies(p1, p5));
        assertEquals("bob", u.get("A").toString());
        assertEquals("ns",  u.get("B").toString());

        // #1ns::bob = B::bob  // fail, a var can not "get" the private ns
        u = new Unifier();
        assertFalse(u.unifies(new LiteralImpl(new Atom(NameSpace.LOCAL_PREFIX+"1ns"), Literal.LPos, "bob"), p5));

        // ns::A = ns::B // A -> B
        u = new Unifier();
        assertTrue(u.unifies(p1, p6));
        assertFalse(u.unifies(new VarTerm("A"), ASSyntax.parseLiteral("ns::bob(45)")));
        //Literal t = (Literal)p1.capply(u);
        //assertEquals("ns::bob(45)", t.toString());

        // ns::A = other::B // fail
        u = new Unifier();
        p6 = ASSyntax.parseLiteral("other::B");
        u.unifies(p1, p6);
        assertFalse(u.unifies(p1, p6));

    }

    public void testApply1() throws ParseException {
        Term t = ASSyntax.parseTerm("A::bob");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        t = t.capply(u);
        assertEquals("ns::bob", t.toString());

        t = ASSyntax.parseTerm("A::~bob(10)[a(20)]");
        u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        t = t.capply(u);
        assertEquals("ns::~bob(10)[a(20)]", t.toString());

        Literal l = ASSyntax.parseLiteral("f(default)");
        assertEquals(l.getTerm(0), Literal.DefaultNS);
        assertTrue(l.getTerm(0) == Literal.DefaultNS);
        Literal p = ASSyntax.parseLiteral("f(NS)");
        u = new Unifier();
        u.unifies(p, l);
        p = (Literal)p.capply(u);
        assertTrue(p.getTerm(0) == Literal.DefaultNS);
    }
    
    public void testApply2() throws ParseException {
        Term t = ASSyntax.parseTerm("A::B");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        Term sa = ASSyntax.parseTerm("kk::b(10)");
        u.unifies(new VarTerm(new Atom("kk"), "B"), sa);
        t = t.capply(u);
        assertEquals(sa.getClass().getName(), t.getClass().getName());
        assertEquals("ns::b(10)", t.toString());

        t = ASSyntax.parseTerm("A::B");
        u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        sa = ASSyntax.parseTerm("b(10) > 8");
        u.unifies(new VarTerm("B"), sa);
        t = t.capply(u);
        assertEquals(sa.getClass().getName(), t.getClass().getName());
        assertEquals("(ns::b(10) > 8)", t.toString());

        t = ASSyntax.parseTerm("A::B");
        u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        sa = ASSyntax.parseTerm(".include(kk)");
        u.unifies(new VarTerm("B"), sa);
        t = t.capply(u);
        assertEquals(sa.getClass().getName(), t.getClass().getName());
        assertEquals("ns::.include(kk)", t.toString());
    
        t = ASSyntax.parseTerm("A::B");
        u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        sa = ASSyntax.parseTerm("[a,kk::b,10]");
        u.unifies(new VarTerm("B"), sa);
        t = t.capply(u);
        assertEquals(sa.getClass().getName(), t.getClass().getName());
        assertEquals("[a,kk::b,10]", t.toString());

        t = ASSyntax.parseTerm("A::B");
        u = new Unifier();
        u.unifies(new VarTerm("A"), new Atom("ns"));
        sa = ASSyntax.parseTerm("{+!g : b <- act; +kk::b(10)}");
        u.unifies(new VarTerm("B"), sa);
        t = t.capply(u);
        assertEquals(sa.getClass().getName(), t.getClass().getName());
        assertEquals("{ +!g : b <- act; +kk::b(10) }", t.toString());
    }
    
    public void testConstants() throws ParseException {
        Unifier u = new Unifier();

        // A = 10 // unifies
        assertTrue(u.unifies(new VarTerm("A"), ASSyntax.parseTerm("10")));
        assertEquals("10", u.get("A").toString());

        // ns1::A = 10 // unifies
        u = new Unifier();
        assertTrue(u.unifies(ASSyntax.parseTerm("ns::A"), ASSyntax.parseTerm("10")));
        assertEquals("10", u.get("A").toString());

        // A::B = 10    // unifies (A -> default)
        u = new Unifier();
        assertTrue(u.unifies(ASSyntax.parseTerm("A::B"), ASSyntax.parseTerm("10")));
        assertEquals("10", u.get("B").toString());

    }

    public void testCompare() throws ParseException {
        Literal p1 = ASSyntax.parseLiteral("ns1::a");
        Literal p2 = ASSyntax.parseLiteral("ns2::b");
        Literal p3 = ASSyntax.parseLiteral("ns2::a");

        //assertNotSame(p1.getPredicateIndicator().hashCode(), p2.getPredicateIndicator().hashCode() );
        //assertNotSame(p1.getPredicateIndicator().hashCode(), p3.getPredicateIndicator().hashCode() );
        assertNotSame(p1.hashCode(), p3.hashCode() );

        assertEquals(p1.compareTo(p2), -1);
        assertEquals(p1.compareTo(null), -1);
        assertEquals(p2.compareTo(p1), 1);
        assertEquals(p1.compareTo(p1), 0);

        p1 = ASSyntax.parseLiteral("ns1::a");
        p2 = ASSyntax.parseLiteral("ns1::b");

        assertEquals(p1.compareTo(p2), -1);
        assertEquals(p1.compareTo(null), -1);
        assertEquals(p2.compareTo(p1), 1);
        assertEquals(p1.compareTo(p1), 0);
    }

    public void testParserNS1() throws ParseException, JasonException {

        as2j parser = new as2j(new StringReader("b(10). b(20). b(tell). ns71::b(30). default::b(40). !g(ok). +!g(X) <- .print(kk, 10, X). "));
        parser.setNS(new Atom("ns33"));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
        a.addInitialBelsInBB();
        assertTrue(a.getBB().toString().contains("ns33::b(10)"));
        assertTrue(a.getBB().toString().contains("ns33::b(20)"));
        assertTrue(a.getBB().toString().contains("ns33::b(tell)")); // tell is reserved word

        //assertTrue(a.getPL().toString().contains("!ns33::g(ns33::X) <- .print(ns33::kk,10,ns33::X)"));
        assertTrue(a.getPL().toString().contains("!ns33::g(X) <- .print(kk,10,X)"));

        //assertTrue(a.getInitialGoals().toString().equals("[ns33::g(ns33::ok)]"));
        assertTrue(a.getInitialGoals().toString().equals("[ns33::g(ok)]"));

        // BB iterator
        int i = 0;
        Iterator<Literal> il = a.getBB().iterator();
        while (il.hasNext()) {
            //Literal l =
            il.next();
            i++;
        }
        assertEquals(5,i);

    }

    public void testParserNS2() throws ParseException, JasonException {

        as2j parser = new as2j(new StringReader("+NS::tick <- .print(NS). "));
        parser.setNS(new Atom("ns33"));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
        //assertTrue(a.getPL().toString().contains("[source(self)] +NS::tick <- .print(ns33::NS)."));
        assertTrue(a.getPL().toString().contains("[source(self)] +NS::tick <- .print(NS)."));
    }

    public void testParserNS3() throws ParseException, JasonException {
        as2j parser = new as2j(new StringReader("-!NoPlan[error(no_relevant), k,b,source(AgenteAdversario)] <- .print(NS). "));
        //parser.setNS(new Atom("ns33"));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
        System.out.println(a.getPL());
        assertTrue(a.getPL().toString().contains("-!NoPlan[b,k,error(no_relevant),source(AgenteAdversario)] <- .print(NS)"));
    }

    public void testDirective() throws ParseException, JasonException {
        as2j parser = new as2j(new StringReader(
                                   "{begin namespace(ns1)}\n"+
                                   "  b1(t). "+
                                   "  +tick <- .print(t); !g(u). "+
                                   "  +!ttt :  bel(k,X)[a1] =.. [A,B,C,D] <- .print(A,B,C,D). "+
                                   "{end}\n"+
                                   "{begin namespace(ns2,local)}\n"+
                                   "  b4(t). "+
                                   "  +tk <- .print(t); +ns1::tick; !g5(u). "+
                                   "{end}\n"+
                                   "b5(i)."+
                                   "{namespace(ns3,local)}\n"+
                                   "b2(t). +!g2(V) <- +b3(h); +ns1::tick; +ns2::tk; +ns3::b5(ns2::t). "
                               ));

        Agent a = new Agent();
        a.initAg();
        parser.setNS(new Atom("nsd"));
        parser.agent(a);
        a.addInitialBelsInBB();
        //System.out.println(a.getBB());
        for (Plan p: a.getPL())
            System.out.println(p);
        //assertTrue(a.getPL().toString().contains("+ns1::tick <- .print(ns1::t); !ns1::g(ns1::u)"));
        //assertTrue(a.getPL().toString().contains("+#1ns2::tk <- .print(#1ns2::t); +ns1::tick; !#1ns2::g5(#1ns2::u)"));
        //assertTrue(a.getPL().toString().contains("+!nsd::g2(nsd::V) <- +nsd::b3(nsd::h); +ns1::tick; +#1ns2::tk; +#2ns3::b5(#1ns2::t)"));

        assertTrue(a.getPL().toString().contains("+ns1::tick <- .print(t); !ns1::g(u)"));
        assertTrue(a.getPL().toString().contains("+!ns1::ttt : (ns1::bel(k,X)[a1] =.. [A,B,C,D]) <- .print(A,B,C,D).")); // should not prefix elements of the list with ns
        assertTrue(a.getPL().toString().contains("ns2::tk <- .print(t); +ns1::tick; !#"));
        assertTrue(a.getPL().toString().contains("+!nsd::g2(V) <- +nsd::b3(h); +ns1::tick; +#"));
        assertTrue(a.getPL().toString().contains("ns2::tk; +#"));

    }

    public void testAbolish() throws ParseException, JasonException {
        as2j parser = new as2j(new StringReader("b(10). b(20). b(tell). ns71::b(30). default::b(40). !g(ok). +!g(X) <- .print(kk, 10, X). "));
        parser.setNS(new Atom("ns3"));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
        a.addInitialBelsInBB();
        a.abolish(ASSyntax.parseLiteral("ns3::_"), null);
        assertEquals(2, a.getBB().size());
    }
    
}
