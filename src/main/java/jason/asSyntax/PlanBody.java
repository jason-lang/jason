package jason.asSyntax;


/**
 * Interface for elements of a plans's body.
 * 
 * @opt nodefillcolor lightgoldenrodyellow
 */
public interface PlanBody extends Term {

    public enum BodyType {
        none {            public String toString() { return ""; }},
        action {          public String toString() { return ""; }},
        internalAction {  public String toString() { return ""; }},
        achieve {         public String toString() { return "!"; }},
        test {            public String toString() { return "?"; }},
        addBel {          public String toString() { return "+"; }},
        addBelNewFocus {  public String toString() { return "++"; }},        
        addBelBegin {     public String toString() { return "+<"; }},       // equivalent to simple +    
        addBelEnd {       public String toString() { return "+>"; }},      
        delBel {          public String toString() { return "-"; }},
        delAddBel {       public String toString() { return "-+"; }},
        achieveNF {       public String toString() { return "!!"; }},
        constraint {      public String toString() { return ""; }}
    }

    public BodyType    getBodyType();
    public Term        getBodyTerm();
    public PlanBody    getBodyNext();

    public boolean     isEmptyBody();
    public int         getPlanSize();

    public void setBodyType(BodyType bt);
    public void setBodyTerm(Term t);
    public void setBodyNext(PlanBody bl);
    public PlanBody getLastBody();
    
    public boolean isBodyTerm();
    public void setAsBodyTerm(boolean b);    
    
    public boolean add(PlanBody bl);
    public boolean add(int index, PlanBody bl);
    public Term removeBody(int index);  
    
    /** clone the plan body */
    public PlanBody clonePB(); 
}
