package jason.asSyntax;


/** 
 * @deprecated use PlanBodyImpl instead.
 * 
 * @hidden
 */
public class BodyLiteral extends PlanBodyImpl {

    /** @deprecated Use BodyType of PlanBody instead */
    public enum BodyType {
        none {            public String toString() { return ""; }},
        action {          public String toString() { return ""; }},
        internalAction {  public String toString() { return ""; }},
        achieve {         public String toString() { return "!"; }},
        test {            public String toString() { return "?"; }},
        addBel {          public String toString() { return "+"; }},
        delBel {          public String toString() { return "-"; }},
        delAddBel {       public String toString() { return "-+"; }},
        achieveNF {       public String toString() { return "!!"; }},
        constraint {      public String toString() { return ""; }}
    }

    public BodyLiteral(BodyType t, Term b) {
        super(oldToNew(t),b);
    }
    
    private static PlanBody.BodyType oldToNew(BodyType old) {
        switch (old) {
        case action: return PlanBody.BodyType.action;
        case internalAction: return PlanBody.BodyType.internalAction;
        case achieve: return PlanBody.BodyType.achieve;
        case test: return PlanBody.BodyType.test;
        case addBel: return PlanBody.BodyType.addBel;
        case delBel: return PlanBody.BodyType.delBel;
        case delAddBel: return PlanBody.BodyType.delAddBel;
        case achieveNF: return PlanBody.BodyType.achieveNF;
        case constraint: return PlanBody.BodyType.constraint;
        default: break;
        }
        return PlanBody.BodyType.none;
    }

}
