package jason.infra.centralised;

public enum RConf {
    THREADED("threaded"),
    POOL_SYNCH("pool"),
    POOL_SYNCH_SCHEDULED("synch_scheduled"),
    ASYNCH("asynch"),
    ASYNCH_SHARED_POOLS("asynch_shared");

    private String text;

    RConf(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }

    public static RConf fromString(String text) {
        if (text != null) {
            for (RConf b : RConf.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return RConf.THREADED;
    }
}
