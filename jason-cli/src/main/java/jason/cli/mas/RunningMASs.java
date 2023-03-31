package jason.cli.mas;

import jason.infra.local.RunLocalMAS;
import jason.runtime.RuntimeServices;
import jason.runtime.RuntimeServicesFactory;

import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** handle the current/known MAS that are running */
public class RunningMASs {

    protected static RunLocalMAS localRunningMAS = null;

    public static void setLocalRunningMAS(RunLocalMAS r) {
        localRunningMAS = r;
    }
    public static RunLocalMAS getLocalRunningMAS() {
        return localRunningMAS;
    }

    public static boolean hasLocalRunningMAS() {
        return localRunningMAS != null && localRunningMAS.isRunning();
    }

    public static boolean isRunningMAS(String masName) {
        var rt = getRTS(masName);
        if  (rt == null)
            return false;
        else {
            try {
                return rt.isRunning();
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    public static RuntimeServices getRTS(String masName) {
        if (masName == null || masName.isEmpty()) {
            return RuntimeServicesFactory.get();
        }
        //  find remote runtime service by RMI
        try {
            var rt = (RuntimeServices) LocateRegistry.getRegistry().lookup(RunLocalMAS.RMI_PREFIX_RTS+masName);
            if (rt.isRunning()) {
                return rt;
            }
        } catch (Exception e) {
        }

        //  try with list of running MAS
        try {
            var addr = getAllRunningMAS().get(masName);
            if  (addr != null && !addr.isEmpty()) {
                // try to connect
                var saddr = addr.split(":");
                var rt = (RuntimeServices)LocateRegistry.getRegistry(saddr[0], Integer.parseInt(saddr[1])).lookup(RunLocalMAS.RMI_PREFIX_RTS+masName);

                if (rt.isRunning()) {
                    return rt;
                }
            }
        } catch (Exception e) {
        }

        return null;
    }
    public static boolean hasAgent(String masName, String agName) {
        var rt = getRTS(masName);
        if  (rt == null)
            return false;
        else {
            try {
                return rt.getAgStatus(agName) != null;
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    public static Map<String,String> getAllRunningMAS() {
        var map = getRMIRunningMAS();
        var all = testAllRemoteMAS();
        for (var mas: all.keySet()) {
            if (mas.equals("latest___mas"))
                continue;
            map.put(mas.toString(), all.getProperty(mas.toString()));
        }
        if (isRunningMAS(null)) {
            map.put(localRunningMAS.getProject().getSocName(), "local");
        }
        return map;
    }

    public static Map<String,String> getRMIRunningMAS() {
        var map = new HashMap<String, String>();
        try {
            var reg = LocateRegistry.getRegistry();
            for (String r : reg.list()) {
                if (r.startsWith(RunLocalMAS.RMI_PREFIX_RTS)) {
                    map.put(r.substring(RunLocalMAS.RMI_PREFIX_RTS.length()).trim(), "rmi");
                }
            }
        } catch (java.rmi.ConnectException e) {
            // no rmi running, ok
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Properties testAllRemoteMAS() {
        var props = new Properties();
        var f = RunLocalMAS.getRunningMASFile();
        if (f.exists()) {
            try {
                props.load(new FileReader(f));
            } catch (IOException e) {
                return props;
            }
            boolean changed = false;
            var localMAS = "";
            if (localRunningMAS != null)
                localMAS = localRunningMAS.getProject().getSocName();
            for (var masName: props.keySet()) {
                if (masName.equals(localMAS))
                    continue;
                if (masName.equals("latest___mas"))
                    continue;
                var addr = props.getProperty(masName.toString());
                var saddr = addr.split(":");
                try {
                    // try to connect
                    //System.out.println("** try "+addr+" for "+masName);
                    var rt = (RuntimeServices)LocateRegistry.getRegistry(saddr[0], Integer.parseInt(saddr[1])).lookup(RunLocalMAS.RMI_PREFIX_RTS+masName);

                    if (!rt.isRunning()) {
                        props.remove(masName);
                        changed = true;
                    }
                } catch (Exception e) {
                    props.remove(masName);
                    changed = true;
                }
            }

            if (changed) {
                try {
                    // TODO uncomment props.store(new FileWriter(f),"running mas in jason");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return props;
    }
}

