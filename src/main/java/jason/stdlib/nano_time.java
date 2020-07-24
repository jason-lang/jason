package jason.stdlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Atom;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServices;
import jason.runtime.RuntimeServicesFactory;
import jason.runtime.Settings;
import java.io.File;
import jason.asSyntax.NumberTermImpl;

/**
  <p>Internal action: <b><code>.list_files</code></b>.

  <p>Description: lists files of a folder

  <p>Parameters:<ul>

  <li>+ path (string): path to be listed.<br/>

  <li><i>+ pattern</i> (string -- optional): a regular expression file names should match.<br/>

  </ul>

  <p>Examples:<ul>

  <li> <code>.list_files("./",R)</code>: unify in R the list of all files in the working directory and its
  subdirectories.</li>

  <li> <code>.list_files("./src/agt",".*.asl",L)</code>: unify in L the list of all '.asl' files in the directory
  '.src/agt/' and its subdirectories (e.g. [./src/agt/bob.asl, .src/agt/alice.asl]) </li>

  <li> <code>.list_files("/media/movies",".*[(][0-9]{4}[)].*",L)</code>: unify in L the
  list of all files in '/media/movies' in which the name contains 4 numbers between parenthesis.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.save_agent
*/
@Manual(
        literal=".create_agent(name[,source,customisations])",
        hint="lists files of a folder",
        argsHint= {
                "path to be listed",
                "a regular expression file names should match [optional]"
        },
        argsType= {
                "string",
                "string"
        },
        examples= {
                ".list_files(\"./\",R): unifies R with the list of all files in the working directory and subdirectories",
                ".list_files(\"./src/agt\",\".*.asl\",L): unifies L with the list of all '.asl' files in the directory and subdirectories",
                ".list_files(\"/media/movies\",\".*[(][0-9]{4}[)].*\",L): unifies L with the list of all files in '/media/movies' in which the name contains 4 numbers between parenthesis."
        },
        seeAlso= {
                "jason.stdlib.create_agent",
                "jason.stdlib.save_agent"
        }
    )
@SuppressWarnings("serial")
public class nano_time extends DefaultInternalAction {

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
      checkArguments(args);

      return  un.unifies(args[0], new NumberTermImpl(System.nanoTime()));
    }
}
