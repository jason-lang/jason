package jason.util;


public class Pair<T1,T2> implements Comparable<Pair<T1,T2>> {

    final T1 o1;
    final T2 o2;
    int hc = 0;

    public Pair(T1 o1, T2 o2) {
        this.o1 = o1;
        this.o2 = o2;
        if (o1 != null) hc =+ o1.hashCode();
        if (o2 != null) hc =+ o2.hashCode();
        hc = hc * 31;
    }

    public T1 getFirst() {
        return o1;
    }

    public T2 getSecond() {
        return o2;
    }

    @Override
    public int hashCode() {
        return hc;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Pair) {
            Pair o = (Pair)obj;
            return o.o1.equals(this.o1) && o.o2.equals(this.o2);
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(Pair<T1, T2> arg) {
        int c = ((Comparable)o1).compareTo(arg.o1);
        if (c == 0) {
            return ((Comparable)o2).compareTo(arg.o2);
        } else {
            return c;
        }
    }

    @Override
    public String toString() {
        return "<"+o1+","+o2+">";
    }
}
