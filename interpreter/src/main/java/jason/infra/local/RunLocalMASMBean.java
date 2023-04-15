package jason.infra.local;

public interface RunLocalMASMBean {
    public int     getNbAgents();
    public boolean killAg(String agName);
    public void    finish(int deadline, boolean stopJVM, int exitValue);
}
