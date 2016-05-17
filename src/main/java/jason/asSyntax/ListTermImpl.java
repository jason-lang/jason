// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a list node as in prolog .(t1,.(t2,.(t3,.))).
 * 
 * Each nth-ListTerm has both a term and the next ListTerm.
 * The last ListTem is an empty ListTerm (term==null).
 * In lists terms with a tail ([a|X]), next is the Tail (next==X, term==a).
 *
 * @navassoc - element - Term
 * @navassoc - next - ListTerm
 *
 * @author Jomi
 */
public class ListTermImpl extends Structure implements ListTerm {
    
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(ListTermImpl.class.getName());

    public static final String LIST_FUNCTOR = ".";
    private Term term;
    private Term next;
    
    public ListTermImpl() {
        super(LIST_FUNCTOR, 0);
    }
    
    private ListTermImpl(Term t, Term n) {
        super(LIST_FUNCTOR, 0);
        term = t;
        next = n;
    }

    public static ListTerm parseList(String sList) {
        as2j parser = new as2j(new StringReader(sList));
        try {
            return parser.list();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing list "+sList,e);
            return null;
        }
    }
    
    /** make a hard copy of the terms */
    public ListTerm clone() {
        ListTermImpl t = new ListTermImpl();
        if (term != null) t.term = this.term.clone();
        if (next != null) t.next = this.next.clone();
        t.hashCodeCache = this.hashCodeCache;
        return t;
    }
    
    /** make a hard copy of the terms */
    public ListTerm cloneLT() {
        return clone();
    }

    /** make a hard copy of the terms */
    @Override
    public ListTerm capply(Unifier u) {
        ListTermImpl t = new ListTermImpl();
        if (term != null) t.term = this.term.capply(u);
        if (next != null) t.next = this.next.capply(u);
        return t;
    }

    /** make a shallow copy of the list (terms are not cloned, only the structure) */
    public ListTerm cloneLTShallow() {
        ListTermImpl t = new ListTermImpl();
        if (term != null) t.term = this.term;
        if (next != null) t.next = this.next.clone();
        return t;
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;

        if (t instanceof Term &&  ((Term)t).isVar() ) return false; // unground var is not equals a list
        if (t instanceof ListTerm) {
            ListTerm tAsList = (ListTerm)t;
            if (term == null && tAsList.getTerm() != null) return false;
            if (term != null && !term.equals(tAsList.getTerm())) return false;
            if (next == null && tAsList.getNext() != null) return false;
            if (next != null) return next.equals(tAsList.getNext());
            return true;
        } 
        return false;
    }
    
    @Override
    public int calcHashCode() {
        int code = 37;
        if (term != null) code += term.hashCode();
        if (next != null) code += next.hashCode();
        return code;
    }
    
    @Override
    public int compareTo(Term o) {
        if (o instanceof VarTerm) 
            return o.compareTo(this) * -1;
        if ((o instanceof NumberTerm)) 
            return 1;
        if (o instanceof StringTerm) 
            return 1;
        return super.compareTo(o);
    }
    
    public void setTerm(Term t) {
        term = t;
    }
    
    /** gets the term of this ListTerm */
    public Term getTerm() {
        return term;
    }
    
    public void setNext(Term l) {
        next = l;
    }
    
    public ListTerm getNext() {
        if (next instanceof ListTerm)
            return (ListTerm)next;
        else
            return null;
    }
    
    // for unifier compatibility
    @Override
    public int getArity() {
        if (isEmpty()) {
            return 0;
        } else {
            return 2; // term and next
        }
    }
    
    // for unifier compatibility
    @Override
    public Term getTerm(int i) {
        if (i == 0) return term;
        if (i == 1) return next;
        return null;
    }

    // for unifier compatibility
    @Override
    public void setTerm(int i, Term t) {
        if (i == 0) term = t;
        if (i == 1) next = t;
    }
    
    /** return the this ListTerm elements (0=Term, 1=ListTerm) */
    public List<Term> getTerms() {
        logger.warning("Do not use getTerms in lists!");
        List<Term> l = new ArrayList<Term>(2);
        if (term != null) l.add(term);
        if (next != null) l.add(next);
        return l;
    }
    
    public void addTerm(Term t) {
        logger.warning("Do not use addTerm in lists! Use add(Term).");
    }

    public int size() {
        if (isEmpty()) {
            return 0;
        } else if (isTail()) {
            return 1;
        } else {
            return getNext().size() + 1;
        }
    }
    
    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    public boolean isEmpty() {
        return term == null;
    }
    public boolean isEnd() {
        return isEmpty() || isTail();
    }

    public boolean isGround() {
        if (isEmpty()) {
            return true;
        } else if (isTail()) {
            return false;
        } else if (term != null && term.isGround()) {
            return getNext().isGround();
        }
        return false;
    }

    /*
    @Override
    public boolean apply(Unifier u) {
        if (isEmpty()) {
            return false;
        } else if (term != null) {
            boolean rn = term.apply(u);
            boolean rt = getNext().apply(u);
            return rn || rt;
        }
        return false;
    }
    */

    @Override
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        logger.log(Level.WARNING, "ListTermImpl cannot be used for logical consequence!", new Exception());
        return LogExpr.EMPTY_UNIF_LIST.iterator();
    }

    public boolean isTail() {
        return next != null && next.isVar();
    }
    
    /** returns this ListTerm's tail element in case the List has the Tail, otherwise, returns null */
    public VarTerm getTail() {
        if (isTail()) {
            return (VarTerm)next;
        } else if (next != null) {
            return getNext().getTail();
        } else {
            return null;
        }
    }
    
    /** set the tail of this list */
    public void setTail(VarTerm v) {
        if (getNext().isEmpty())
            next = v;
        else
            getNext().setTail(v);
    }
    
    /** get the last ListTerm of this List */
    public ListTerm getLast() {
        ListTerm r = this;
        while (!r.isEnd() && r.getNext() != null)
            r = r.getNext();
        return r;
        /* recursive implementation 
        if (isEnd()) {
            return this;
        } else if (next != null) {
            return getNext().getLast();
        } 
        return null; // !!! no last!!!!
        */
    }
    
    public ListTerm getPenultimate() {
        if (getNext() == null)
            return null;
        if (isTail())
            return this;
        if (getNext().isEnd() && !getNext().isTail())
            return this;
        return getNext().getPenultimate();
    }
    
    public Term removeLast() {
        ListTerm p = getPenultimate();
        if (p != null) {
            Term b = p.getTerm();
            p.setTerm(null);
            p.setNext(null);
            return b;
        } else {
            return null;
        }
    }
    
    /** 
     * Adds a term in the end of the list
     * @return the ListTerm where the term was added (i.e. the last ListTerm of the list)
     */
    public ListTerm append(Term t) {
        if (isEmpty()) {
            term = t;
            next = new ListTermImpl();
            return this;
        } else if (isTail()) {
            // What to do?
            return null;
        } else {
            return getNext().append(t);
        }
    }
    
    /** 
     * insert a term in the begin of this list
     * @return the new starter of the list
     */
    public ListTerm insert(Term t) {
        ListTerm n = new ListTermImpl(term,next);
        this.term = t;
        this.next = n;
        return n;
    }

    /** 
     * Adds a list in the end of this list.
     * This method do not clone <i>lt</i>.
     * @return the last ListTerm of the new list
     */
    public ListTerm concat(ListTerm lt) {
        if (isEmpty()) {
            setValuesFrom(lt);
        } else if (((ListTerm)next).isEmpty() ) {
            next = lt;
        } else {
            ((ListTerm)next).concat(lt);
        }
        return lt.getLast();
    }
    
    /**
     * Creates a new (cloned) list with the same elements of this list, but in the reversed order.
     * The Tail remains the Tail: reverse([a,b|T]) = [b,a|T].
     */
    public ListTerm reverse() {
        return reverse_internal(new ListTermImpl());
    }
    private ListTerm reverse_internal(ListTerm r) {
        if (isEmpty()) {
            return r;
        } else if (isTail()) {
            r = new ListTermImpl(term.clone(), r);
            r.setTail((VarTerm)next.clone());
            return r;
        } else {
            return ((ListTermImpl)next).reverse_internal( new ListTermImpl(term.clone(), r) );
        }
    }

    /** returns a new (cloned) list representing the set resulting of the union of this list and lt. */
    public ListTerm union(ListTerm lt) {
        Set<Term> set = new TreeSet<Term>();
        set.addAll(lt);
        set.addAll(this);
        return setToList(set);
    }

    /** returns a new (cloned) list representing the set resulting of the intersection of this list and lt. */
    public ListTerm intersection(ListTerm lt) {
        Set<Term> set = new TreeSet<Term>();
        set.addAll(lt);
        set.retainAll(this);
        return setToList(set);
    }
    
    /** returns a new (cloned) list representing the set resulting of the difference of this list and lt. */
    public ListTerm difference(ListTerm lt) {
        Set<Term> set = new TreeSet<Term>();
        set.addAll(this);
        set.removeAll(lt);
        return setToList(set);
    }

    // copy the set to a new list
    private ListTerm setToList(Set<Term> set) {
        ListTerm result = new ListTermImpl();
        ListTerm tail = result;
        for (Term t: set)
            tail = tail.append(t.clone());
        return result;
    }

    /** returns all subsets that take k elements of this list */
    public Iterator<List<Term>> subSets(final int k) {
        // based on a DFS algorithm
        return new Iterator<List<Term>>() {
            LinkedList<SubSetSearchState> open = null;
            Term[] thisAsArray = new Term[0];
            
            List<Term> next = null;
            
            public boolean hasNext() {
                if (open == null) {
                    open = new LinkedList<SubSetSearchState>(); // states to explore
                    //open.add(new SubSetSearchState(new ArrayList<Term>(), getAsList(), k)); // initial state (root of search tree)
                    thisAsArray = getAsList().toArray(thisAsArray);
                    open.add(new SubSetSearchState(0, k, null, null)); // initial state (root of search tree)
                }
                if (next == null) {
                    getNext();
                }
                return next != null;
            }
            
            public List<Term> next() {
                if (next == null)
                    getNext();
                List<Term> r = next;
                next = null;
                return r;
            }
            
            void getNext() {
                while (! open.isEmpty() ) {
                    SubSetSearchState s = open.removeFirst();
                    if (s.d == 0) {
                        next = s.getAsList();
                        return;
                    } else {
                        s.addNexts();
                    }
                }
                next = null;
            }
            
            public void remove() { }
            
            class SubSetSearchState {
                int pos;
                int d;  
                Term value = null;
                SubSetSearchState f = null;
                
                SubSetSearchState(int pos, int d, Term t, SubSetSearchState father) {  
                    this.pos = pos; this.d = d; this.value = t; this.f = father; 
                }
                void addNexts() {
                    int pSize = (k-d)+thisAsArray.length;
                    for (int i = thisAsArray.length-1; i >= pos; i--) {
                        if (pSize-i >= k) {
                            open.addFirst(new SubSetSearchState(i+1, d-1, thisAsArray[i], this));
                        } 
                    }
                }
                
                List<Term> getAsList() {
                    LinkedList<Term> np = new LinkedList<Term>();
                    SubSetSearchState c = this;
                    while (c.value != null) {
                        np.addFirst(c.value);
                        c = c.f;
                    }
                    return np;
                }
            }
            
            /*// old code
            class SubSetSearchState {
                List<Term> prefix, elements; 
                int d;  
                SubSetSearchState(List<Term> p, List<Term> e, int d) {  
                    prefix = p; elements = e; this.d = d;  
                }
                void addNexts(List<SubSetSearchState> open) {
                    int esize = elements.size();
                    int maxNextSize = prefix.size()+esize;
                    for (int i = esize-1; i >= 0; i--) {
                        if (maxNextSize-i >= k) {
                            List<Term> np = new ArrayList<Term>(prefix);
                            np.add(elements.get(i));
                            open.add(0, new SubSetSearchState(np, elements.subList(i+1, esize), d-1));
                        }
                    }
                }
            }
            */
        };
    }
    
    /*
    public List<List<Term>> subSets(int k) {
        List<List<Term>> result = new ArrayList<List<Term>>();
        generateSubSets(new ArrayList<Term>(), getAsList(), k, result);
        return result;
    }
    private static void generateSubSets(List<Term> prefix, List<Term> elements, int k, List<List<Term>> result) {
        if (k == 0) {
            result.add(prefix);
        } else {
            int esize = elements.size();
            for (int i = 0; i < esize; i++) {
                List<Term> np = new ArrayList<Term>(prefix); // prepare new prefix
                np.add(elements.get(i));
                generateSubSets(np, elements.subList(i+1, esize), k-1, result);
            }
        }
    }
    */
    
    /*
    public List<List<Term>> subSets(int k, Set<PredicateIndicator> types) {
        List<List<Term>> result = new ArrayList<List<Term>>();
        List<Term> annots = new ArrayList<Term>();
        for (Term t: this) 
            if (t.isLiteral()) {
                if (types.contains( ((Literal)t).getPredicateIndicator() ))
                    annots.add(t);
            } else {
                annots.add(t);                
            }
        
        generateSubSets(new ArrayList<Term>(), annots, k, result);
        return result;
    }     
     */
    /** 
     * gives an iterator that includes the final empty list or tail, 
     * for [a,b,c] returns [a,b,c]; [b,c]; [c]; and [].
     * for [a,b|T] returns [a,b|T]; [b|T]; [b|T]; and T.
     */
    public Iterator<ListTerm> listTermIterator() {
        return new ListTermIterator<ListTerm>(this) {
            public ListTerm next() {
                moveNext();
                return current;
            }
        };
    }
    

    /** 
     * returns an iterator where each element is a Term of this list,
     * the tail of the list is not considered. 
     * for [a,b,c] returns 'a', 'b', and 'c'.
     * for [a,b|T] returns 'a' and 'b'.
     */  
    public Iterator<Term> iterator() {
        return new ListTermIterator<Term>(this) {
            public boolean hasNext() {
                return nextLT != null && !nextLT.isEmpty() && nextLT.isList(); 
            }
            public Term next() {
                moveNext();
                return current.getTerm();
            }
        };
    }
        
    private abstract class ListTermIterator<T> implements Iterator<T> {
        ListTerm nextLT;
        ListTerm current = null;
        public ListTermIterator(ListTerm lt) {
            nextLT = lt;
        }
        public boolean hasNext() {
            return nextLT != null; 
        }
        public void moveNext() {
            current = nextLT;
            nextLT  = nextLT.getNext();
        }
        public void remove() {
            if (current != null && nextLT != null) {
                current.setTerm(nextLT.getTerm());
                current.setNext(nextLT.getNext());
                nextLT = current;
            }
        }
    }


    /** 
     * Returns this ListTerm as a Java List (implemented by ArrayList). 
     * Note: the tail of the list, if any, is not included!
     */
    public List<Term> getAsList() {
        List<Term> l = new ArrayList<Term>();
        for (Term t: this)
            l.add(t);
        return l;
    }

    
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        ListTerm l = this;
        while (!l.isEmpty()) {
            s.append(l.getTerm());
            if (l.isTail()) {
                s.append('|');
                s.append(l.getTail());
                break;
            }
            l = l.getNext();
            if (l == null)
                break;
            if (!l.isEmpty())
                s.append(',');
        }
        s.append(']');
        return s.toString();
    }

    //
    // Java List interface methods
    //
    
    public void add(int index, Term o) {
        if (index == 0) {
            insert(o);
        } else if (index > 0 && getNext() != null) {
            getNext().add(index-1,o);
        }
    }
    public boolean add(Term o) {
        return getLast().append(o) != null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean addAll(Collection c) {
        if (c == null) return false;
        ListTerm lt = this; // where to add
        Iterator<Term> i = c.iterator();
        while (i.hasNext()) {
            lt = lt.append(i.next());
        }
        return true;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean addAll(int index, Collection c) {
        Iterator<Term> i = c.iterator();
        int p = index;
        while (i.hasNext()) {
            add(p, i.next()); 
            p++;
        }
        return true;
    }
    public void clear() {
        term = null;
        next = null;
    }

    public boolean contains(Object o) {
        if (term != null && term.equals(o)) {
            return true;
        } else if (getNext() != null) {
            return getNext().contains(o);
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean containsAll(Collection c) {
        boolean r = true;
        Iterator<Term> i = c.iterator();
        while (i.hasNext() && r) {
            r = r && contains(i.next()); 
        }
        return r;
    }

    public Term get(int index) {
        if (index == 0) {
            return this.term;
        } else if (getNext() != null) {
            return getNext().get(index-1);
        }
        return null;
    }

    public int indexOf(Object o) {
        if (this.term.equals(o)) {
            return 0;
        } else if (getNext() != null) {
            int n = getNext().indexOf(o);
            if (n >= 0) {
                return n+1;
            }
        }
        return -1;
    }
    public int lastIndexOf(Object arg0) {
        return getAsList().lastIndexOf(arg0);
    }

    public ListIterator<Term> listIterator() {
        return listIterator(0);
    }
    
    // TODO: do not base the implementation of listIterator on get (that is O(n))
    // conversely, implement all other methods of List based on this iterator
    // (see AbstractSequentialList)
    // merge code of ListTermIterator here and use always the same iterator
    public ListIterator<Term> listIterator(final int startIndex) {
        final ListTermImpl list = this;
        return new ListIterator<Term>() {
            int pos = startIndex;
            int last = -1;
            int size = size();

            public void add(Term o) {
                list.add(last,o);
            }
            public boolean hasNext() {
                return pos < size;
            }
            public boolean hasPrevious() {
                return pos > startIndex;
            }
            public Term next() {
                last = pos;
                pos++;
                return get(last);
            }
            public int nextIndex() {
                return pos+1;
            }
            public Term previous() {
                last = pos;
                pos--;
                return get(last);
            }
            public int previousIndex() {
                return pos-1;
            }
            public void remove() {
                list.remove(last);
            }
            public void set(Term o) {
                remove();
                add(o);
            }            
        };
    }

    protected void setValuesFrom(ListTerm lt) {
        this.term = lt.getTerm();
        this.next = lt.getNext();
    }
    
    public Term remove(int index) {
        if (index == 0) {
            Term bt = this.term;
            if (getNext() != null) {
                setValuesFrom(getNext());
            } else {
                clear();
            }
            return bt;
        } else if (getNext() != null) {
            return getNext().remove(index-1);
        }
        return null;
    }

    public boolean remove(Object o) {
        if (term != null && term.equals(o)) {
            if (getNext() != null) {
                setValuesFrom(getNext());
            } else {
                clear();
            }
            return true;
        } else if (getNext() != null) {
            return getNext().remove(o);
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes" })
    public boolean removeAll(Collection c) {
        boolean r = true;
        Iterator i = c.iterator();
        while (i.hasNext() && r) {
            r = r && remove(i.next()); 
        }
        return r;
    }

    @SuppressWarnings({ "rawtypes" })
    public boolean retainAll(Collection c) {
        boolean r = true;
        Iterator i = iterator();
        while (i.hasNext()) {
            Term t = (Term)i.next();
            if (!c.contains(t)) {
                r = r && remove(t);
            }
        }
        return r;
    }

    public Term set(int index, Term t) {
        if (index == 0) {
            this.term = (Term)t;
            return t;
        } else if (getNext() != null) {
            return getNext().set(index-1, t);
        }
        return null;
    }

    public List<Term> subList(int arg0, int arg1) {
        return getAsList().subList(arg0, arg1);
    }

    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int s = size();
        if (a.length < s)
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), s);

        int i = 0;
        for (Term t: this) {
            a[i++] = (T)t;
        }
        if (a.length > s)
            a[s] = null;

        return a;
    }
    
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("list-term");
        String c = "";
        for (Term t: this) {
            Element et = t.getAsDOM(document);
            et.setAttribute("sep", c);
            c = ",";
            u.appendChild(et);
        }
        Term tail = getTail();
        if (tail != null) {
            Element et = tail.getAsDOM(document);
            et.setAttribute("sep", "|");
            u.appendChild(et);
        }
        return u;
    }
}
