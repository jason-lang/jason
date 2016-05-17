package jason.asSyntax;

/** The interface for string terms of the AgentSpeak language
 *  
 * @opt nodefillcolor lightgoldenrodyellow
 */
public interface StringTerm extends Term {
    /** gets the Java string represented by this term, it  
        normally does not return the same string as toString 
        (which enclose the string by quotes)  */
    public String getString();
    public int length();
}
