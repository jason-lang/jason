package jason.stdlib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.type</code></b>.

  <p>Description: retrieves types of the argument.

  <p>Parameter:<ul>
  <li>+ argument (any term): the term to be checked.<br/>
  <li>+/- type (atom or variable): the given type or a variable. Values are atoms.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.type(\"home page\",string)</code>: true.
  <li> <code>.type(b(10),string)</code>: false.
  <li> <code>.type(b,T)</code>: unifies T with the term 'atom' and backtracks unifying T with 'literal' and 'ground'.
  <li> <code>.type(X,T)</code>: unifies T with the term 'free' if X is free, or other types if X is not free.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
@Manual(
        literal=".type(argument,type)",
        hint="retrieves types of the argument or checks whether the argument is of a given type",
        argsHint= {
                "the term to be checked",
                "the given type or a variable to retrieves types"
        },
        argsType= {
                "term",
                "term or variable"
        },
        examples= {
                ".type(\"home page\",string): true",
                ".type(b(10),string): false",
                ".type(b,T): unifies T with the term 'atom' and backtracks unifying T with 'literal' and 'ground'",
                ".type(X,T): unifies T with the term 'free' if X is free, or other types if X is not free"
        },
        seeAlso= {
                "jason.stdlib.atom",
                "jason.stdlib.list",
                "jason.stdlib.literal",
                "jason.stdlib.number",
                "jason.stdlib.string",
                "jason.stdlib.structure",
                "jason.stdlib.ground"
        }
    )
@SuppressWarnings("serial")
public class type extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new type();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        // It is ordered from the most primitive types to less primitive
        List<Term> types = new ArrayList<>();
        if (args[0].isNumeric())    types.add(ASSyntax.createAtom("number"));
        if (args[0].isAtom())       types.add(ASSyntax.createAtom("atom"));
        if (args[0].isLiteral())    types.add(ASSyntax.createAtom("literal"));
        if (args[0].isString())     types.add(ASSyntax.createAtom("string"));
        if (args[0].isList())       types.add(ASSyntax.createAtom("list"));
        if (args[0].isSet())        types.add(ASSyntax.createAtom("set"));
        if (args[0].isMap())        types.add(ASSyntax.createAtom("map"));
        if (args[0].isStructure())  types.add(ASSyntax.createAtom("structure"));
        if (args[0].isRule())       types.add(ASSyntax.createAtom("rule"));
        if (args[0].isPlanBody())   types.add(ASSyntax.createAtom("plan"));
        if (args[0] instanceof ObjectTerm && ((ObjectTerm)args[0]).getObject() instanceof Queue) {
            types.add(ASSyntax.createAtom("queue"));
        }

        if (args[0].isGround())     types.add(ASSyntax.createAtom("ground"));
        if (args[0].isVar())        types.add(ASSyntax.createAtom("free"));


        // if it just wants to check whether args[0] is of type args[1]
        if (!args[1].isVar()) {
            return types.contains(args[1]);
        } else {
            // backtracks all types of args[0]
            return new Iterator<Unifier>() {
                int n = 0;

                public boolean hasNext() {
                    return (n < types.size());
                }

                public Unifier next() {
                    Unifier c = un.clone();
                    c.unifies(args[1], types.get(n++));
                    return c;
                }
            };
        }
    }
}
