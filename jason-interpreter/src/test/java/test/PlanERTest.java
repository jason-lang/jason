package test;

import java.util.List;

import org.junit.Before;

import jason.JasonException;
import jason.asSemantics.Event;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.pl.PlanLibrary;
import jason.asSyntax.parser.ParseException;
import junit.framework.TestCase;

public class PlanERTest extends TestCase {

    String code;

    @Before
    public void setup() {
        code = "+!p(X) : X > 0 <: X > 100 | .done  <- .print(a). {"
                + " +e <- x. "
                + " +!sg : a <: false <- k. "
                + " +!sg <- k. { +x <- l. +e <- m.} "
                +" } ";
    }

    public void testParser() throws JasonException, ParseException {
        setup();
        Plan p;
        try {
            p = ASSyntax.parsePlan("+!p(X) : X > 0 { <- .print(a). ");
            assertFalse(true);
        } catch (ParseException e) {
            // ok
        }

        p = Plan.parse("+!p(X) : X > 0 <- .print(a). ");
        assertEquals("+!p(X) : (X > 0) <- .print(a).", p.toString());

        p = Plan.parse("+!p(X) : X > 0 <: X > 100 | .done <- .print(a). ");
        assertEquals("+!p(X) : (X > 0) <: ((X > 100) | .done) <- .print(a).", p.toString());

    }

    public void testProperties() {
        setup();
        Plan p = Plan.parse(code);
        assertTrue(p.hasInterestInUpdateEvents());
        assertEquals(3, p.getSubPlans().size());
        //True(p.toString().startsWith("+!p(X) : (X > 0) <: ((X > 100) | .done) {\n   <- .print(a).\n   +e <- x.\n   +!sg : a <: false <- k."));

        Plan sg2 = p.getSubPlans().getPlans().get(2);
        assertTrue(sg2.hasInterestInUpdateEvents());
    }

    public void testCandidates() throws JasonException, ParseException {
        setup();
        PlanLibrary pl = new PlanLibrary();
        Plan        p = Plan.parse(code);
        pl.add(p);

        Event e = new Event(
                ASSyntax.parseTrigger("+!sg"),
                null);

        List<Plan> cand = pl.getCandidatePlans(e.getTrigger());
        assertNull(cand);

        cand = p.getSubPlans().getCandidatePlans(e.getTrigger());
        //System.out.println(cand);
        assertEquals(2, cand.size());

        p = cand.get(0);
        if (!p.hasSubPlans())
            p = cand.get(1);

        e = new Event(ASSyntax.parseTrigger("+e"), null);
        cand = p.getSubPlans().getCandidatePlans(e.getTrigger());
        assertEquals(1, cand.size());
    }
}
