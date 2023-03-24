package jason.runtime;

public class RuntimeServicesFactory {
    static private RuntimeServices singleton = null;
    static public void set(RuntimeServices s) {
        singleton = s;
    }
    static public RuntimeServices get() { return singleton; }
}
