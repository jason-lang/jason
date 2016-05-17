package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class StringTermTest extends TestCase {

    public void testParsing() {
        StringTerm s = StringTermImpl.parseString("\"a\"");
        assertEquals(s.getString(), "a");
        assertEquals(s.toString(), "\"a\"");
        assertEquals(s, new StringTermImpl("a"));
        
        //s = StringTermImpl.parseString("\"a(\\\\\"k\\\\\")\"");
        //System.out.println(s);
        //assertEquals(s.getString(), "a(\"k\")");
    }
    
    public void testUnify() {
        Term t1 = new StringTermImpl("bla");
        Term t2 = new StringTermImpl("bla");
        Term t3 = new StringTermImpl("notbla");
        
        Unifier u = new Unifier();
        assertTrue(u.unifies(t1, t2));
        assertFalse(u.unifies(t1, t3));
        
        Term v1 = new VarTerm("X");
        assertTrue(u.unifies(t1, v1));
        assertTrue(u.unifies(t2, v1));
        assertFalse(u.unifies(v1, t3));
        
    }
}
