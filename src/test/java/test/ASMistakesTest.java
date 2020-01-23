package test;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ASMistakesTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testOpenParenthesis() {
        Throwable e = null;
        try {
            ASSyntax.parseTerm("{ +!p <- .my_name(_34Me }");
            fail("An expected exception did not occurred.");
        } catch(Throwable ex) {
            e = ex;
        }
        assertTrue(e instanceof ParseException);
    }

    public void testMultilineComment() {
        Throwable e = null;
        try {
            ASSyntax.parseTerm("{ +!p <- .print(Hello) /* comment * }");
            fail("An expected exception did not occurred.");
        } catch(Throwable ex) {
            e = ex;
        }
        assertTrue(e instanceof ParseException);
    }

    public void testOpenQuotes() {
        Throwable e = null;
        try {
            ASSyntax.parseTerm("{ +!p <- .print(\"Hello) }");
            fail("An expected exception did not occurred.");
        } catch(Throwable ex) {
            e = ex;
        }
        assertTrue(e instanceof jason.asSyntax.parser.TokenMgrError);
    }
}
