package jason.asSyntax;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an unnamed variable '_'.
 *
 * @author jomi
 */
public class UnnamedVar extends VarTerm {

    private static final long serialVersionUID = 1L;

    private static AtomicInteger varCont = new AtomicInteger(0);
    public int myId;

    public UnnamedVar() {
        this(Literal.DefaultNS, varCont.incrementAndGet());
    }

    public UnnamedVar(Atom ns) {
        this(ns, varCont.incrementAndGet());
    }
    public UnnamedVar(Atom ns, int id) {
        super(ns, "_" + id);
        myId = id;
    }

    public UnnamedVar(int id) {
        super("_" + id);
        myId = id;
    }

    // do not allow the creation of unnamed var by name since the myId attribute should be defined!
    // this constructor is for internal use (see create below)
    private UnnamedVar(String name) {
        super(name);
    }
    private UnnamedVar(Atom ns, String name) {
        super(ns, name);
    }

    public static UnnamedVar create(String name) {
        return create(Literal.DefaultNS, name);
    }
    public static UnnamedVar create(Atom ns, String name) {
        if (name.length() == 1) { // the case of "_"
            return new UnnamedVar(ns);
        } else {
            int id = varCont.incrementAndGet();
            UnnamedVar v = new UnnamedVar(ns, "_"+id+name);
            v.myId = id;
            return v;
        }
    }

    public Term clone() {
        return cloneNS(getNS());
    }

    @Override
    public Literal cloneNS(Atom newNameSpace) {
        UnnamedVar newv = new UnnamedVar(newNameSpace, getFunctor());
        newv.myId = this.myId;
        newv.hashCodeCache = this.hashCodeCache;
        if (hasAnnot())
            newv.addAnnots(this.getAnnots().cloneLT());
        return newv;
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;
        if (t instanceof UnnamedVar) return ((UnnamedVar)t).myId == this.myId;
        return false;
    }

    public int compareTo(Term t) {
        if (t instanceof UnnamedVar) {
            if (myId > ((UnnamedVar)t).myId)
                return 1;
            else if (myId < ((UnnamedVar)t).myId)
                return -1;
            else
                return 0;
        } else if (t instanceof VarTerm) {
            return 1;
        } else {
            return super.compareTo(t);
        }
    }

    @Override
    public boolean isUnnamedVar() {
        return true;
    }
}
