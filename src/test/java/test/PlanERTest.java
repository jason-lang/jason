package test;

import jason.JasonException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.asSyntax.parser.ParseException;
import junit.framework.TestCase;

public class PlanERTest extends TestCase {

    public void testParser() throws JasonException, ParseException {
        Plan p;
        try {
            p = ASSyntax.parsePlan("+!p(X) : X > 0 { <- .print(a). ");
            assertFalse(true);
        } catch (ParseException e) {
            // ok
        }
        
        p = Plan.parse("+!p(X) : X > 0 { <- .print(a). } ");
        assertEquals("+!p(X) : (X > 0) <- .print(a).", p.toString());
        
        p = Plan.parse("+!p(X) : X > 0 <: X > 100 | .done { <- .print(a). } ");
        assertEquals("+!p(X) : (X > 0) <: ((X > 100) | .done) <- .print(a).", p.toString());
        
        p = Plan.parse("+!p(X) : X > 0 <: X > 100 | .done { <- .print(a). "
                + " +e <- x. "
                + " +!sg : a <: false <- k. "
                + " +!sg { <- k. +x <- l. +y <- m.} "
                +" } ");
        assertTrue(p.toString().startsWith("+!p(X) : (X > 0) <: ((X > 100) | .done) {\n   <- .print(a).\n   +e <- x.\n   +!sg : a <: false <- k."));
    }
}
