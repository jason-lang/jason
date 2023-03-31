package test;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;
import static jason.asSyntax.ASSyntax.createString;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.RelExpr;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.asSyntax.parser.as2jConstants;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.mas2j;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ASParserTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testNegVar() throws ParseException {
        Literal l = ASSyntax.parseLiteral("~B");
        assertTrue(l.isVar());
        assertTrue(l.negated());
        l = (Literal)l.clone();
        assertTrue(l.isVar());
        assertTrue(l.negated());
        Literal l1 = ASSyntax.parseLiteral("~B");
        Literal l2 = ASSyntax.parseLiteral("B");
        assertTrue(l1.equals(l2)); // see comment in varterm equals
    }

    public void testTrue() throws ParseException {
        Term t = ASSyntax.parseTerm("true");
        assertEquals("jason.asSyntax.Literal$TrueLiteral", t.getClass().getName());
        t = ASSyntax.parseTerm("false");
        assertEquals("jason.asSyntax.Literal$FalseLiteral", t.getClass().getName());
    }

    public void testKQML() throws Exception {
        Agent ag = new Agent();
        ag.initAg();

        ag.parseAS(new File("src/main/resources/asl/kqmlPlans.asl"));
        ag.parseAS(new File("src/test/java/jason/asl/ag1.asl"));
        Plan p = ag.getPL().get("lbid");
        //System.out.println(ag.getPL());
        assertNotNull(p);
        assertEquals(p.getBody().getPlanSize(), 1);
        assertEquals(((PlanBody)p.getBody()).getBodyType(), PlanBody.BodyType.internalAction);
        ag.parseAS(new File("src/test/java/jason/asl/ag3.asl"));
    }

    public void testLogicalExpr() throws Exception {
        LogicalFormula t1 = LogExpr.parseExpr("(3 + 5) / 2 > 0");
        assertTrue(t1 != null);
        Iterator<Unifier> solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("0 > ((3 + 5) / 2)");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("0 > (-5)");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(((((((30) + -5)))))) / 2 > 5+3*8");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("-2 > -3");
        assertTrue(t1 != null);
        RelExpr r1 = (RelExpr) t1;
        NumberTerm lt1 = (NumberTerm)r1.getTerm(0);
        NumberTerm rt1 = (NumberTerm)r1.getTerm(1);
        assertEquals(-2.0,lt1.solve());
        assertEquals(-3.0,rt1.solve());
        //System.out.println(lt1.getClass().getName()+"="+lt1.compareTo(rt1));
        solve = r1.logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > (-1 + -2)");
        assertTrue(t1 != null);
        solve = ((RelExpr)t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > -3 & 2 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > -3 & 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > -3 | 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 > 5) | false");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 > 5) | true");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        // Teste antigo - Não faz sentido na estrutura atual do Jason
        // t1 = LogExpr.parseExpr("not not (not 3 > 5)");
        // assertTrue(t1 != null);
        // solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        // assertTrue(solve.hasNext());

        as2j parser = new as2j(new StringReader("E+1"));
        parser.getNextToken();
        assertEquals(parser.token.kind, as2jConstants.VAR);
        t1 = ASSyntax.parseFormula("E+1");
        assertTrue(t1 instanceof ArithExpr);
    }

    public void testArithExpr() throws Exception {
        NumberTerm t1 = ArithExpr.parseExpr("1 - 1 - 1");
        assertTrue(t1 != null);
        assertEquals(t1.solve(), -1.0);

        t1 = ArithExpr.parseExpr("1 - 1 - 1 -1");
        assertTrue(t1 != null);
        assertEquals(t1.solve(), -2.0);

        t1 = ArithExpr.parseExpr("1 - (1 - 1)");
        assertTrue(t1 != null);
        assertEquals(t1.solve(), 1.0);

        t1 = ArithExpr.parseExpr("8 / 4 / 2");
        assertTrue(t1 != null);
        assertEquals(t1.solve(), 1.0);

        t1 = ArithExpr.parseExpr("-0.5 * 2");
        assertTrue(t1 != null);
        assertEquals(t1.solve(), -1.0);

    }

    public void testDirectives() throws Exception {
        String
        source =  " b(10). ";
        source += " { begin bc(at(X,Y), ebdg(at(X,Y))) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X,Y). \n";
        source += "    +!at(X,Y) : not b(X) <- go(3,Y). \n";
        source += " { end }";

        as2j parser = new as2j(new StringReader(source));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
        assertEquals(7, a.getPL().getPlans().size());

        source =  " { begin omc(at(X,Y), no_battery, no_beer) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X,Y). ";
        source += "    @l1 +!at(X,left(H)) : not b(X) <- go(3,Y). ";
        source += " { end }";
        parser = new as2j(new StringReader(source));
        a = new Agent();
        a.initAg();
        a.setASLSrc("test1");
        parser.agent(a);
        assertEquals(10, a.getPL().getPlans().size());
        assertEquals("@l1[source(self)] +!at(X,left(H)) : not (b(X)) <- go(3,Y); ?at(X,left(H)).", a.getPL().get("l1").toString());

        source =  " { begin mg(at(10,10)) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X,Y). ";
        source += "    +!at(X,Y) : not b(X) <- go(3,Y). ";
        source += " { end }";
        parser = new as2j(new StringReader(source));
        a = new Agent();
        a.initAg();
        parser.agent(a);
        //for (Plan p: a.getPL().getPlans()) {
        //    System.out.println(p);
        //}
        assertTrue(a.getPL().getPlans().size() == 8);
        assertTrue(a.getInitialBels().size() == 1);

        source =  " { begin sga(\"+go(X,Y)\", \"(at(home) & not c)\", at(X,Y)) } \n";
        source += " { end }";
        parser = new as2j(new StringReader(source));
        a = new Agent();
        a.initAg();
        parser.agent(a);
        //for (Plan p: a.getPL().getPlans()) {
        //    System.out.println(p);
        //}
        assertEquals(5, a.getPL().getPlans().size());

    }

    public void testParsingPlanBodyTerm1() throws ParseException {
        Literal l = ASSyntax.parseLiteral("p( {a1({f}); a2}, a3, {!g}, {?b;.print(oi) }, 10)");
        assertEquals("p({ a1({ f }); a2 },a3,{ !g },{ ?b; .print(oi) },10)", l.toString());
        assertEquals(5,l.getArity());
        assertTrue(l.getTerm(0) instanceof PlanBody);
        assertTrue(l.getTerm(0).isPlanBody());
        PlanBody pb = (PlanBody)l.getTerm(0);
        assertTrue(pb.isBodyTerm());
        Structure t0 = (Structure)pb.getBodyTerm();
        assertTrue(t0.getTerm(0).isPlanBody());
        pb = pb.getBodyNext();
        assertEquals("{ a2 }", pb.toString());

        assertFalse(l.getTerm(1).isPlanBody());
        assertTrue(l.getTerm(2).isPlanBody());
        assertTrue(l.getTerm(3).isPlanBody());
        assertFalse(l.getTerm(4).isPlanBody());

        pb = (PlanBody)ASSyntax.parseTerm("{ }");
        assertEquals(0, pb.getPlanSize());

        pb = (PlanBody)ASSyntax.parseTerm("{ a; B}");
        assertEquals(2, pb.getPlanSize());
        assertEquals(BodyType.action, pb.getBodyType());
        assertEquals(BodyType.action, pb.getBodyNext().getBodyType()); // B must be considered as action, see TS
    }


    public void testParsingPlanBodyTerm2() throws ParseException {
        Unifier un = new Unifier();
        Term t = ASSyntax.parseTerm("{ +a(10) }");
        assertTrue(t.isPlanBody());
        assertEquals("{ +a(10) }", t.toString()); // plan terms should not have default annotations

        // t = ASSyntax.parseTerm("{ @label(a,b,10,test,long,label) +a(10) }");
        // assertTrue(t instanceof Plan);
        // assertEquals("{ @label(a,b,10,test,long,label) +a(10) }", t.toString());

        t = ASSyntax.parseTerm("{ -+a(10) }");
        assertTrue(t.isPlanBody());
        assertEquals("{ -+a(10) }", t.toString());

        t = ASSyntax.parseTerm("{ -a; +b }");
        assertEquals("{ -a; +b }", t.toString());
        assertTrue(t.isPlanBody());
        PlanBody pb = (PlanBody)t;
        assertEquals(2, pb.getPlanSize());

        t = ASSyntax.parseTerm("{ -a : b <- c1; c2 }");
        assertTrue(t instanceof Plan);
        assertEquals("{ -a : b <- c1; c2 }", t.toString());

        t = ASSyntax.parseTerm("{ +!a(10) }");
        assertTrue(t instanceof Trigger);
        assertEquals("{ +!a(10) }", t.toString());
        assertTrue(t.isStructure());
        Structure s = (Structure)t;
        assertEquals(2, s.getArity());
        assertEquals("te", s.getFunctor());
        Term te = ASSyntax.parseTerm("{ +!a(X) }");
        assertTrue(un.unifies(t,te));

        t = ASSyntax.parseTerm("{ !a }");
        assertTrue(t.isPlanBody());
        pb = (PlanBody)t;
        assertEquals(1, pb.getPlanSize());
        assertEquals("a", pb.getBodyTerm().toString());
        assertEquals(PlanBody.BodyType.achieve, pb.getBodyType());

        t = ASSyntax.parseTerm("{ +!a <- +b }");
        assertEquals("{ +!a <- +b }", t.toString());
        assertTrue(t.isStructure());
        s = (Structure)t;
        assertEquals(4, s.getArity());
        assertEquals("plan", s.getFunctor());

        t = ASSyntax.parseTerm("{ +a <- +c }");
        assertEquals("{ +a <- +c }", t.toString());
        assertTrue(t.isStructure());
        s = (Structure)t;
        assertEquals(4, s.getArity());
        assertEquals("plan", s.getFunctor());
        assertEquals(Literal.LTrue, s.getTerm(2));
    }

    public void testParsingPlanBodyStephenBug() throws ParseException {
        Plan t = (Plan)ASSyntax.parseTerm("{ +!say_hello <- .my_name(_34Me); .println(_34Me) }");
        assertTrue(t.isPlanTerm());
        assertEquals("{ +!say_hello <- .my_name(_34Me); .println(_34Me) }", t.toString());
        UnnamedVar v1 = new UnnamedVar(34);
        UnnamedVar v2 = new UnnamedVar(-34);
        assertFalse( ((Literal)t.getBody().getBodyNext().getBodyTerm()).getTerm(0).equals(v1) );
        assertTrue(  ((Literal)t.getBody().getBodyNext().getBodyTerm()).getTerm(0).equals(v2) );
    }

    public void testParsingAllSources() {
        parseDir(new File("../examples"));
        parseDir(new File("../demos"));
    }

    public void parseDir(File dir) {
        if (!dir.exists())
            return;
        if (dir.getName().endsWith(".svn"))
            return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f: files) {
                try {
                    if (f.isDirectory()) {
                        parseDir(f);
                    } else if (f.getName().endsWith(MAS2JProject.AS_EXT)) {
                        if (f.getName().equals("search.asl"))  continue; // ignore this file
                        if (f.getName().equals("b.asl"))  continue; // ignore this file
                        System.out.println("parsing "+f);
                        as2j parser = new as2j(new FileInputStream(f));
                        Agent ag = new Agent();
                        ag.initAg();
                        parser.agent(ag);
                    } else if (f.getName().endsWith(MAS2JProject.EXT)) {
                        mas2j parser = new mas2j(new FileInputStream(f));
                        parser.mas();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Error parsing "+f+": "+e);
                }
            }
        }
    }

    public void testFactory() throws ParseException {
        // create the literal 'p'
        Literal l = createLiteral("p");
        assertEquals("p", l.toString());

        // create the literal 'p(a,3)'
        l = createLiteral("p", createAtom("a"), createNumber(3));
        assertEquals("p(a,3)", l.toString());

        // create the literal 'p(a,3)[s,"s"]'
        l = createLiteral("p", createAtom("a"), createNumber(3))
            .addAnnots(createAtom("s"), createString("s"));
        assertEquals("p(a,3)[\"s\",s]", l.toString());

        // create the literal '~p(a,3)[s,"s"]'
        l = createLiteral(Literal.LNeg, "p", createAtom("a"), createNumber(3))
            .addAnnots(createAtom("s"), createString("s"));
        assertEquals("~p(a,3)[\"s\",s]", l.toString());
        l = ASSyntax.parseLiteral(l.toString());
        assertEquals("~p(a,3)[\"s\",s]", l.toString());

        ListTerm   ll = ASSyntax.createList(); // empty list
        assertEquals("[]", ll.toString());

        ll = ASSyntax.createList(createAtom("a"), createLiteral("b", createNumber(5)));
        assertEquals("[a,b(5)]", ll.toString());

        ll = ASSyntax.parseList(ll.toString());
        assertEquals("[a,b(5)]", ll.toString());
    }

    public void testParserPartial() throws ParseException {
        Term t;
        try {
            t = ASSyntax.parseFormula("(X>1) & (X<3) (Y>5) & (X<7)");
            assertTrue(t == null);
        } catch (ParseException e) {
            // ok, the exception must be thrown above
        }
        t = ASSyntax.parseFormula("(X>1) & (X<3) | (Y>5) & (X<7)");
        assertEquals("(((X > 1) & (X < 3)) | ((Y > 5) & (X < 7)))", t.toString());
    }

    public void testBugSC() throws Exception {
        String source = "@foo1 +!g <- .print(foo1).\n" +
                        "@foo2 +!g <- .print(foo2).\n";

        as2j parser = new as2j(new StringReader(source));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
    }

    public void testElIf() throws ParseException, JasonException {
        String source =
        "+!g(Mode,Team) <- \n" +
        "    if (Mode<=2) { \n"+
        "          .println(\"I found some object.\"); \n" +
        "    } else {\n" +
        "            .nth(1, ObjectFound, Team);\n" +
        "            if (Team == 200) {\n" +
        "                .println( \"if\" );\n" +
        "            }\n" +
        "            elif (Team == 100) { \n" +
        "                .println( \"elif\" );\n" +
        "            }\n" +
        "            else {\n" +
        "                .println( \"else\" );\n" +
        "            }\n" +
        "    }.  // End of else";
        as2j parser = new as2j(new StringReader(source));
        Agent a = new Agent();
        a.initAg();
        parser.agent(a);
    }

    public void testBodyTermLeftUnif() throws ParseException, JasonException {
        String source =   "{ NewBodyTerm;_ } = { !gg(Name, SubstitutedSubGoals) }  ";
        LogicalFormula f = ASSyntax.parseFormula( source) ;
        assertNotNull(f);

        assertEquals("([a,b,c] = [a,b,c])", ASSyntax.parseFormula("[a,b,c] = [a,b,c]").toString());
    }

    public void testVarAnnots() throws ParseException, JasonException {
        Term v = ASSyntax.parseTerm("achieve(_[H|Annots1])");
        assertTrue(v.toString().contains("[H|Annots1])"));
    }
}
