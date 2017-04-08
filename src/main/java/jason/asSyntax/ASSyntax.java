package jason.asSyntax;

import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.asSyntax.parser.as2jConstants;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
  Factory for objects used in Jason AgentSpeak syntax.

  <p><b>Examples of Term</b>:
  <pre>
  import static jason.asSyntax.ASSyntax.*;

  ...
  Atom       a = createAtom("a");
  NumberTerm n = createNumber(3);
  StringTerm s = createString("s");
  Structure  t = createStructure("p", createAtom("a")); // t = p(a)
  ListTerm   l = createList(); // empty list
  ListTerm   f = createList(createAtom("a"), createStructure("b", createNumber(5))); // f = [a,b(5)]

  // or use a parsing (easier but slower)
  Term n = parseTerm("5");
  Term t = parseTerm("p(a)");
  Term l = parseTerm("[a,b(5)]");
  </pre>

  <p><b>Examples of Literal</b>:
  <pre>
  import static jason.asSyntax.ASSyntax.*;

  ...
  // create the literal 'p'
  Literal l1 = createLiteral("p");

  // create the literal 'p(a,3)'
  Literal l2 = createLiteral("p", createAtom("a"), createNumber(3));

  // create the literal 'p(a,3)[s,"s"]'
  Literal l3 = createLiteral("p", createAtom("a"), createNumber(3))
                            .addAnnots(createAtom("s"), createString("s"));

  // create the literal '~p(a,3)[s,"s"]'
  Literal l4 = createLiteral(Literal.LNeg, "p", createAtom("a"), createNumber(3))
                            .addAnnots(createAtom("s"), createString("s"));

  // or use the parser (easier but slower)
  Literal l4 = parseLiteral("~p(a,3)[s]");
  </pre>

  @hidden

  @author Jomi

 */
public class ASSyntax {

    public static final String hardDeadLineStr = "hard_deadline";

    private static Set<PredicateIndicator> keywords = new HashSet<PredicateIndicator>();

    static {
        keywords.add(new PredicateIndicator("atomic", 0));
        keywords.add(new PredicateIndicator("breakpoint", 0));
        keywords.add(new PredicateIndicator("all_unifs", 0));
        keywords.add(new PredicateIndicator("default", 0));

        keywords.add(new PredicateIndicator("this_ns", 0));

        keywords.add(new PredicateIndicator("source", 1));
        keywords.add(new PredicateIndicator("self", 0));
        keywords.add(new PredicateIndicator("percept", 0));

        keywords.add(new PredicateIndicator("tell", 0));
        keywords.add(new PredicateIndicator("untell", 0));
        keywords.add(new PredicateIndicator("achieve", 0));
        keywords.add(new PredicateIndicator("unachieve", 0));
        keywords.add(new PredicateIndicator("askOne", 0));
        keywords.add(new PredicateIndicator("askAll", 0));
        keywords.add(new PredicateIndicator("askHow", 0));
        keywords.add(new PredicateIndicator("tellHow", 0));
        keywords.add(new PredicateIndicator("untellHow", 0));

    }

    public static void addKeyword(PredicateIndicator pi) {
        keywords.add(pi);
    }

    public static boolean isKeyword(Literal l) {
        return keywords.contains( l.getPredicateIndicator() );
    }

    // ----
    // ---- createX methods
    // ----


    /**
     * Creates a new positive literal, the first argument is the functor (a string)
     * and the n remainder arguments are terms. see documentation of this
     * class for examples of use.
     */
    public static Literal createLiteral(String functor, Term... terms) {
        return new LiteralImpl(functor).addTerms(terms);
    }
    public static Literal createLiteral(Atom namespace, String functor, Term... terms) {
        return new LiteralImpl(namespace, Literal.LPos, functor).addTerms(terms);
    }

    /**
     * Creates a new literal, the first argument is either Literal.LPos or Literal.LNeg,
     * the second is the functor (a string),
     * and the n remainder arguments are terms. see documentation of this
     * class for examples of use.
     */
    public static Literal createLiteral(boolean positive, String functor, Term... terms) {
        return new LiteralImpl(positive, functor).addTerms(terms);
    }

    /**
     * Creates a new literal, the first argument is the namespace, the second is either Literal.LPos or Literal.LNeg,
     * the third is the functor (a string),
     * and the n remainder arguments are terms. see documentation of this
     * class for examples of use.
     */
    public static Literal createLiteral(Atom namespace, boolean positive, String functor, Term... terms) {
        return new LiteralImpl(namespace, positive, functor).addTerms(terms);
    }


    /**
     * Creates a new structure (compound) term, the first argument is the functor (a string),
     * and the n remainder arguments are terms.
     */
    public static Structure createStructure(String functor, Term... terms) {
        int size = (terms == null || terms.length == 0 ? 3 : terms.length);
        return (Structure)new Structure(functor, size).addTerms(terms);
    }

    /** creates a new Atom term (an atom is a structure with 0-arity) */
    public static Atom createAtom(String functor) {
        return new Atom(functor);
    }

    /** creates a new number term */
    public static NumberTerm createNumber(double vl) {
        return new NumberTermImpl(vl);
    }

    /** creates a new string term */
    public static StringTerm createString(String s) {
        return new StringTermImpl(s);
    }
    /** creates a new string term using .toString() of the parameter */
    public static StringTerm createString(Object o) {
        return new StringTermImpl(o.toString());
    }

    /** creates a new variable term */
    public static VarTerm createVar(String functor) {
        return new VarTerm(functor);
    }

    /** creates a new variable term in a namespace */
    public static VarTerm createVar(Atom namespace, String functor) {
        return new VarTerm(namespace, functor);
    }

    /** creates a new variable term (possibly negated) */
    public static VarTerm createVar(boolean negated, String functor) {
        VarTerm v = new VarTerm(functor);
        v.setNegated(negated);
        return v;
    }

    /** creates a new anonymous (or unnamed) variable  */
    public static VarTerm createVar() {
        return new UnnamedVar();
    }

    /** Creates a new list with n elements, n can be 0 */
    public static ListTerm createList(Term... terms) {
        ListTerm l = new ListTermImpl();
        ListTerm tail = l;
        for (Term t: terms)
            tail = tail.append(t);
        return l;
    }

    /** Creates a new list from a collection of terms (each element of the collection is cloned) */
    public static ListTerm createList(Collection<Term> terms) {
        ListTerm l = new ListTermImpl();
        ListTerm tail = l;
        for (Term t: terms)
            tail = tail.append(t.clone());
        return l;
    }


    /** Creates a new rule with a head and a body */
    public static Rule createRule(Literal head, LogicalFormula body) {
        return new Rule(head,body);
    }

    // ----
    // ---- parseX methods
    // ----

    /** creates a new literal by parsing a string */
    public static Literal parseLiteral(String sLiteral) throws ParseException {
        //return new as2j(new StringReader(sLiteral)).literal();
        as2j parser = new as2j(new StringReader(sLiteral));
        Literal l = parser.literal();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+l+" for parameter '"+sLiteral+"'");
        return l;
    }

    /** creates a new number term by parsing a string */
    public static NumberTerm parseNumber(String sNumber) throws NumberFormatException {
        return new NumberTermImpl(Double.parseDouble(sNumber));
    }

    /** creates a new structure (a kind of term) by parsing a string */
    public static Structure parseStructure(String sStructure) throws ParseException {
        as2j parser = new as2j(new StringReader(sStructure));
        Term t = parser.term();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+t+" for parameter '"+sStructure+"'");
        if (t instanceof Structure)
            return (Structure)t;
        else
            return new Structure((Literal)t);
    }

    /** creates a new variable by parsing a string */
    public static VarTerm parseVar(String sVar) throws ParseException {
        //return new as2j(new StringReader(sVar)).var();
        as2j parser = new as2j(new StringReader(sVar));
        VarTerm v = parser.var(Literal.DefaultNS);
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+v+" for parameter '"+sVar+"'");
        return v;
    }

    /** creates a new term by parsing a string */
    public static Term parseTerm(String sTerm) throws ParseException {
        //return new as2j(new StringReader(sTerm)).term();
        as2j parser = new as2j(new StringReader(sTerm));
        Term t = parser.term();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+t+" for parameter '"+sTerm+"'");
        return t;
    }

    /** creates a new plan by parsing a string */
    public static Plan parsePlan(String sPlan) throws ParseException {
        //return new as2j(new StringReader(sPlan)).plan();
        as2j parser = new as2j(new StringReader(sPlan));
        Plan p = parser.plan();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+p+" for parameter '"+sPlan+"'");
        return p;
    }

    /** creates a new plan body by parsing a string */
    public static PlanBody parsePlanBody(String sPlanBody) throws ParseException {
        //return new as2j(new StringReader(sPlan)).plan();
        as2j parser = new as2j(new StringReader(sPlanBody));
        PlanBody p = parser.plan_body();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+p+" for parameter '"+sPlanBody+"'");
        return p;
    }

    /** creates a new trigger by parsing a string */
    public static Trigger parseTrigger(String sTe) throws ParseException {
        //return new as2j(new StringReader(sTe)).trigger();
        as2j parser = new as2j(new StringReader(sTe));
        Trigger te = parser.trigger();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+te+" for parameter '"+sTe+"'");
        return te;
    }

    /** creates a new list by parsing a string */
    public static ListTerm parseList(String sList) throws ParseException {
        //return new as2j(new StringReader(sList)).list();
        as2j parser = new as2j(new StringReader(sList));
        ListTerm l = parser.list();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+l+" for parameter '"+sList+"'");
        return l;
    }

    /** creates a new logical formula by parsing a string */
    public static LogicalFormula parseFormula(String sExpr) throws ParseException {
        //return (LogicalFormula)new as2j(new StringReader(sExpr)).log_expr();
        as2j parser = new as2j(new StringReader(sExpr));
        LogicalFormula l = (LogicalFormula)parser.log_expr();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+l+" for parameter '"+sExpr+"'");
        return l;
    }

    /** creates a new rule by parsing a string */
    public static Rule parseRule(String sRule) throws ParseException {
        //return (Rule)new as2j(new StringReader(sRule)).belief();
        as2j parser = new as2j(new StringReader(sRule));
        Rule r = (Rule)parser.belief();
        if (parser.getNextToken().kind != as2jConstants.EOF)
            throw new ParseException("Expected <EOF> after "+r+" for parameter '"+sRule+"'");
        return r;
    }
}
