package jason.cli.mas;

import jason.infra.local.RunLocalMAS;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;

import java.io.StringReader;

public class CLILocalMAS extends RunLocalMAS implements Runnable {

    @Override
    public int init(String[] args) {
        runner = this;
        //System.out.println("*** My class loader: "+this.getClass().getClassLoader());
        RunningMASs.setLocalRunningMAS(this);
        var r = super.init(args);
        if (project.getSocName() == null || project.getSocName().isEmpty() || project.getSocName().equals("default"))
            project.setSocName(initArgs.get("masName").toString());
        registerMBean();
        registerInRMI();
        registerWebMindInspector();

        var envName = initArgs.get("envName").toString();
        if (!envName.isEmpty()) {
            var parser = new mas2j(new StringReader(envName));
            try {
                project.setEnvClass(parser.classDef());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    public String getName() {
        return project.getSocName();
    }

    @Override
    public void run() {
        super.start();
        waitEnd();
    }

    public void waitEnd() {
        super.waitEnd();
        finish(0, true, 0);
    }

    @Override
    public void finish() {
        super.finish();
        RunningMASs.setLocalRunningMAS(null);
        System.out.println(getName()+" stopped");
    }

    @Override
    public void finish(int deadline, boolean stopJVM, int exitValue) {
        if (deadline != 0)
            System.out.println("Stopping "+getName()+" in "+deadline+" ms...");
        super.finish(deadline, stopJVM, exitValue);
        RunningMASs.setLocalRunningMAS(null);
        System.out.println(getName()+" stopped");
    }
}
