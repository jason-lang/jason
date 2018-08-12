package jason.stdlib;

import jason.asSemantics.InternalAction;


/**
  <p>Internal action: <b><code>.print</code></b>.

  <p>Description: used for printing messages to the console where the system
  is running. It receives any number of parameters, which can be not only
  strings but also any AgentSpeak term (including variables). Terms are made
  ground according to the current unifying function before being printed
  out. No new line is printed after the parameters.

  <p> The precise format and output device of the message is defined
  by the Java logging configuration as defined in the
  <code>logging.properties</code> file in the project directory.

  <p>Parameters:<ul>

  <li>+arg[0] ... +arg[n] (any term): the terms to be printed out.<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.print(1,X,"bla")</code>: prints out to the console the
  concatenation of the string representations of the number 1, of the value of
  variable X, and the string "bla".</li>

  </ul>

  @see jason.stdlib.println
  @see jason.stdlib.printf

*/
public class print extends println implements InternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new print();
        return singleton;
    }

    @Override
    protected String getNewLine() {
        return "";
    }
}
