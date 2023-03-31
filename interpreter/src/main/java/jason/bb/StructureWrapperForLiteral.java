package jason.bb;

import java.io.Serializable;

import jason.asSyntax.Literal;

/** a literal that uses equalsAsTerm for equals */
public final class StructureWrapperForLiteral implements Comparable<StructureWrapperForLiteral>, Serializable {
    private static final long serialVersionUID = 2201448644995026018L;

    final private Literal l;
    public StructureWrapperForLiteral(Literal l) {
        this.l = l;
    }
    public int hashCode() {
        return l.hashCode();
    }
    public boolean equals(Object o) {
        return o instanceof StructureWrapperForLiteral && l.equalsAsStructure(((StructureWrapperForLiteral)o).l);
    }
    public String toString() {
        return l.toString();
    }
    public int compareTo(StructureWrapperForLiteral o) {
        return l.compareTo(o.l);
    }
    public Literal getLiteral() {
        return l;
    }
}
