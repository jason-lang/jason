package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ListTermTest extends TestCase {

    ListTerm l1, l2, l3,l4,l5;
    
    protected void setUp() throws Exception {
        super.setUp();
        l1 = ASSyntax.parseList("[a,b,c]");
        l2 = ASSyntax.parseList("[a(1,2),b(r,t)|T]");
        l3 = ASSyntax.parseList("[A|T]");
        l4 = ASSyntax.parseList("[X,b,T]");
        l5 = ASSyntax.parseList("[[b,c]]");
        //System.out.println("l1="+l1+"\nl2="+l2+"\nl3="+l3+"\nl4="+l4);
        //System.out.println("l5="+l5);
    }
    
    public void testPenultimateAndRemoveLast() {
        assertEquals("[c]",l1.getPenultimate().toString());
        assertEquals("[b(r,t)|T]", l2.getPenultimate().toString());
        assertEquals("[T]", l4.getPenultimate().toString());
        
        assertEquals("c",l1.removeLast().toString());
        assertEquals("[a,b]",l1.toString());
        assertEquals("T",l4.removeLast().toString());
        assertEquals("[X,b]",l4.toString());
        
        assertEquals("b(r,t)",l2.removeLast().toString());
        assertEquals("[a(1,2)]",l2.toString());
    }

    public void testSize() {
        assertEquals(l1.size(), 3);
        assertEquals(l2.size(), 2);
        assertEquals(l3.size(), 1);
        assertEquals(l4.size(), 3);
        assertEquals(l5.size(), 1);
        
        ListTerm l = new ListTermImpl(); 
        l.add(new Structure("a"));
        l.add(new Structure("a"));
        l.add(new Structure("a"));
        assertEquals(l.size(), 3);      
        
        assertTrue(l1.isList());
        assertTrue(l2.isList());
        assertTrue(l3.isList());
        assertTrue(l4.isList());
        assertTrue(l5.isList());
    }

    public void testToString() {
        assertEquals("[a,b,c]",l1.toString());
        assertEquals("[a(1,2),b(r,t)|T]",l2.toString());
    }
    
    public void testUnify() {
        assertTrue( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[a,b,c]")));
        assertTrue( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[A,B,C]")));
        assertFalse( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[a,b]")));
        assertFalse( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[a,b,d]")));

        Unifier u2 = new Unifier();
        assertTrue(u2.unifies((Term)l1,new VarTerm("X")));
        //System.out.println("u2="+u2);

        Unifier u3 = new Unifier();
        assertTrue( u3.unifies((Term)l1,(Term)l3));
        assertEquals( ((ListTerm)u3.get("T")).toString(), "[b,c]");
        //System.out.println("u3="+u3);

        Unifier u4 = new Unifier();
        assertTrue(u4.unifies((Term)l1,(Term)l4));
        //System.out.println("u4="+u4);

        Unifier u5 = new Unifier();
        // [a,b,c] = [X|[b,c]]
        ListTerm lt5 = ListTermImpl.parseList("[X|[b,c]]");
        //System.out.println("lt5="+lt5);
        assertTrue(u5.unifies(l1,lt5));
        //System.out.println("u5="+u5);
        
    }
    
    public void testAddRemove() {
        l1.add(new Structure("d"));
        l1.add(0, new Structure("a1"));
        l1.add(1, new Structure("a2"));
        assertEquals(new Structure("a2"), l1.get(1));
        assertEquals(l1.size(), 6);
        
        List<Term> lal = new ArrayList<Term>();
        lal.add(new Structure("b1"));
        lal.add(new Structure("b2"));
        l1.addAll(4, lal);
        assertEquals(l1.size(), 8);
        
        //System.out.println(l1);
        assertEquals(new Structure("a1"), l1.remove(0));
        assertEquals(new Structure("b"), l1.remove(2));
        assertTrue(l1.remove(new Structure("b1")));
        assertTrue(l1.remove(new Structure("d")));
        assertTrue(l1.remove(new Structure("a2")));
        assertEquals(l1.size(), 3);
        
        Iterator<Term> i = l1.iterator();
        while (i.hasNext()) {
            Term t = i.next();
            //System.out.println("-"+t);
            if (t.equals(new Structure("a"))) {
                i.remove();
            }
        }
        assertEquals("[b2,c]", l1.toString());
        i = l1.iterator();
        i.next(); i.next(); // c is the current
        i.remove(); // remove c
        assertEquals("[b2]", l1.toString());
    }
    
    public void testClone() {
        assertEquals(l1.size(), ((ListTerm)l1.clone()).size());
        assertEquals(l1, l1.clone());
    }

    public void testEquals() {
        assertTrue(l1.equals(l1));
        assertTrue(l1.equals(ListTermImpl.parseList("[a,b,c]")));

        assertFalse(l1.equals(l2));
        assertFalse(l1.equals(ListTermImpl.parseList("[a,b,d]")));
    }

    public void testConcat() {
        
        ListTerm la = ListTermImpl.parseList("[a]");
        ListTerm le = ListTermImpl.parseList("[]");
        le.concat(la);
        assertEquals(le.toString(), "[a]");

        le = ListTermImpl.parseList("[]");
        la.concat(le);
        assertEquals(la.toString(), "[a]");
        
        Unifier u = new Unifier();

        // L1 = [x,y]
        VarTerm l1 = new VarTerm("L1");
        u.unifies(l1, ListTermImpl.parseList("[x,y]"));
        
        // L2 = [a|L1]
        VarTerm l2 = new VarTerm("L2");
        u.unifies(l2, ListTermImpl.parseList("[a|L1]"));
        
        // L3 = [b|L2]
        VarTerm l3 = new VarTerm("L3");
        u.unifies(l3, ListTermImpl.parseList("[b|L2]"));

        // L4 = L3
        VarTerm l4 = new VarTerm("L4");
        u.unifies(l4, l3);

        // L5 = [c|L4]
        VarTerm l5 = new VarTerm("L5");
        u.unifies(l5, ListTermImpl.parseList("[c|L4]"));

        // L6 = L5 concat [d]
        VarTerm l6 = new VarTerm("L6");
        u.unifies(l5, l6);
        ListTerm tl6 = (ListTerm)l6.capply(u);
        assertEquals(tl6.toString(), "[c,b,a,x,y]");
        tl6.concat(ListTermImpl.parseList("[d]"));
        assertEquals(tl6.toString(), "[c,b,a,x,y,d]");

        ListTerm lf = ListTermImpl.parseList("[c,b,a,x,y,d]");
        assertTrue(u.unifies(tl6,lf));

        lf = ListTermImpl.parseList("[c,b,a,x,y]");
        ListTerm tl5 = (ListTerm)l5.capply(u);
        assertTrue(u.unifies(tl5,lf));
        
        ListTerm ll = lf.cloneLT();
        ll.concat(lf.cloneLT());
        assertEquals("[c,b,a,x,y,c,b,a,x,y]", ll.toString());
    }
    
    public void testTail() {
        ListTerm lt5 = ListTermImpl.parseList("[a|[b,c]]");
        assertEquals(lt5.size(),3);
        assertEquals(lt5.getTail(), null);
    }
    
    public void testTailUnify() {
        ListTerm lt5 = ListTermImpl.parseList("[H|T]");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("H"), new Atom("a"));
        u.unifies(new VarTerm("T"), ListTermImpl.parseList("[b,c]"));
        lt5 = (ListTerm)lt5.capply(u);
        assertEquals("[a,b,c]",lt5.toString());
    }

    public void testGround() {
        ListTerm l = ListTermImpl.parseList("[c,b,a,x,y,d]");
        assertTrue(l.isGround());

        l = ListTermImpl.parseList("[c,b,a,X,y,d]");
        assertFalse(l.isGround());
        l = ListTermImpl.parseList("[C,b,a,x,y,d]");
        assertFalse(l.isGround());
        l = ListTermImpl.parseList("[c,b,a,x,y,D]");
        assertFalse(l.isGround());
        
        l = ListTermImpl.parseList("[c,b,a,x,y|T]");
        assertFalse(l.isGround());
        
        l = ListTermImpl.parseList("[c|T]");
        assertFalse(l.isGround());
    }
    
    public void testToArray() {
        ListTerm l = ListTermImpl.parseList("[c,b,a,x,y,d]");
        Term[] a = l.toArray(new Term[0]);
        assertEquals(a.length,l.size());
        assertEquals("c",a[0].toString());
        assertEquals("d",a[a.length-1].toString());
    }
    
    public void testListIterator() throws ParseException {
        Iterator<Term> it = l1.iterator();
        assertTrue(it.hasNext()); assertEquals("a", it.next().toString());
        assertTrue(it.hasNext()); assertEquals("b", it.next().toString());
        assertTrue(it.hasNext()); assertEquals("c", it.next().toString());
        assertFalse(it.hasNext()); 
        
        it = l2.iterator();
        assertTrue(it.hasNext()); assertEquals("a(1,2)", it.next().toString());
        assertTrue(it.hasNext()); assertEquals("b(r,t)", it.next().toString());
        assertFalse(it.hasNext());
        
        it = ASSyntax.parseList("[]").iterator();
        assertFalse(it.hasNext());        
        
        StringBuilder s = new StringBuilder();
        Iterator<ListTerm> i = l1.listTermIterator();
        while (i.hasNext()) {
            s.append(i.next());
        }
        assertEquals("[a,b,c][b,c][c][]",s.toString());

        s = new StringBuilder();
        i = l2.listTermIterator();
        while (i.hasNext()) {
            s.append(i.next());
        }
        //System.out.println(s);
        assertEquals("[a(1,2),b(r,t)|T][b(r,t)|T]T",s.toString());
    }
    
    public void testReverse() {
        ListTerm l = ListTermImpl.parseList("[]");
        assertEquals(l, l.reverse());
        assertFalse( l == l.reverse()); // should be cloned
        assertEquals("[]",l.reverse().toString());
        
        l = ListTermImpl.parseList("[a]");
        assertEquals(l, l.reverse());
        assertFalse( l == l.reverse()); // should be cloned
        assertEquals("[a]",l.reverse().toString());

        l = ListTermImpl.parseList("[a,b,c]");
        assertEquals("[c,b,a]",l.reverse().toString());

        l = ListTermImpl.parseList("[a,b,c|T]");
        assertEquals("[c,b,a|T]",l.reverse().toString());
    }
    
    public void testUnion() {
        ListTerm l1 = ListTermImpl.parseList("[]");
        ListTerm l2 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l3 = ListTermImpl.parseList("[a,b,d,e]");
        
        ListTerm l4 = l1.union(l2);
        assertEquals("[a,b,c]", l4.toString());
        assertEquals(l2,l4);
        
        ListTerm l = l4.union(l3);
        assertEquals(5, l.size());
        
        assertEquals(l2.union(l3), l3.union(l2));
    }

    public void testIntersectoin() {
        ListTerm l1 = ListTermImpl.parseList("[]");
        ListTerm l2 = ListTermImpl.parseList("[c,a,b,c]");
        ListTerm l3 = ListTermImpl.parseList("[b,a,d,e]");
        
        ListTerm l4 = l1.intersection(l2);
        assertEquals("[]", l4.toString());
        assertEquals(l1,l4);
        assertEquals(l1.intersection(l2),l2.intersection(l1));
        
        ListTerm l = l2.intersection(l3);
        assertEquals("[a,b]", l.toString());
    }
    
    public void testDifference() {
        ListTerm l1 = ListTermImpl.parseList("[]");
        ListTerm l2 = ListTermImpl.parseList("[c,a,b,c]");
        ListTerm l3 = ListTermImpl.parseList("[b,a,d,e]");
        
        assertEquals("[c]", l2.difference(l3).toString());
        assertEquals("[d,e]", l3.difference(l2).toString());
        assertEquals("[a,b,c]", l2.difference(l1).toString());
        assertEquals("[a,b,d,e]", l3.difference(l1).toString());
        assertEquals("[]", l1.difference(l2).toString());
    }
    
    public void testSubSet() {
        ListTerm l3 = ListTermImpl.parseList("[a,b,c,8]");
        assertEquals("[[a], [b], [c], [8]]", iterator2list(l3.subSets(1)).toString());
        assertEquals("[[a, b], [a, c], [a, 8], [b, c], [b, 8], [c, 8]]", iterator2list(l3.subSets(2)).toString());
        assertEquals("[[a, b, c], [a, b, 8], [a, c, 8], [b, c, 8]]", iterator2list(l3.subSets(3)).toString());
        assertEquals("[[a, b, c, 8]]", iterator2list(l3.subSets(4)).toString());
        
        l3 = ListTermImpl.parseList("[a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12,a13,a14,a15,a16,a17,a18,a19,a20]");
        //for (int i=0; i< 20;i++)
        //    System.out.println(iterator2list(l3.subSets(i+1)).size());
        assertEquals("[[a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20]]",iterator2list(l3.subSets(20)).toString());
        assertEquals(38760, iterator2list(l3.subSets(6)).size());
        assertEquals(38760, iterator2list(l3.subSets(14)).size());
    }
    
    public void testMkVarAn() {
        ListTermImpl l = (ListTermImpl)ListTermImpl.parseList("[use(car,Agent)]");
        l.makeVarsAnnon();
        assertTrue(l.toString().indexOf("_") > 0);
        
        l = (ListTermImpl)ListTermImpl.parseList("[use(car,Agent)]");
        
        VarTerm v = new VarTerm("V");
        Unifier u = new Unifier();
        u.unifies(v, l);
        Literal tv = (Literal)v.capply(u);
        tv.makeVarsAnnon();
        assertTrue(tv.toString().indexOf("_") > 0);
    }
    
    @SuppressWarnings("unchecked")
    List iterator2list(Iterator i) {
        List l = new ArrayList();
        while (i.hasNext())
            l.add(i.next());
        return l;
    }

}
