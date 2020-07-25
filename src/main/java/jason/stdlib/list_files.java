package jason.stdlib;

import java.util.ArrayList;
import java.util.List;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import java.io.File;

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
        literal=".list_files(path,pattern)",
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
public class list_files extends DefaultInternalAction {

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length > 2 && !args[1].isString())
            throw JasonException.createWrongArgument(this, "second argument must be a string");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        final File path = new File(getFolder(args));
        final String pattern = getPattern(args);
        List<Term> fileList = new ArrayList<>();

        listFiles(pattern, path, fileList);

        if (args.length == 2)
            return un.unifies(ASSyntax.createList(fileList), args[1]);
        else
            return un.unifies(ASSyntax.createList(fileList), args[2]);
    }

    protected String getFolder(Term[] args) throws JasonException {
        String folder = null;
        if (args.length > 0) {
            folder = ((StringTerm) args[0]).getString();
        }
        return folder;
    }

    protected String getPattern(Term[] args) throws JasonException {
        String pattern = null;
        if (args.length != 3) {
            pattern = ".*";
        } else {
            pattern = ((StringTerm) args[1]).getString();
        }
        return pattern;
    }

    void listFiles(final String pattern, final File folder, List<Term> fileList) {
        if (folder.listFiles() == null)
            return;
        for (final File file : folder.listFiles()) {
            if (file.isFile()) {
                if (file.getName().matches(pattern)) {
                    fileList.add(new StringTermImpl(file.getPath()));
                }
            }
            if (file.isDirectory()) {
                listFiles(pattern, file, fileList);
            }
        }
    }

}
